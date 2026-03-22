package code.vanilson.marketplace.service;


import code.vanilson.marketplace.config.JwtService;
import code.vanilson.marketplace.controller.auth.AuthenticationRequest;
import code.vanilson.marketplace.controller.auth.AuthenticationResponse;
import code.vanilson.marketplace.controller.auth.RegisterRequest;
import code.vanilson.marketplace.exception.EmailNotFoundException;
import code.vanilson.marketplace.exception.IncorrectPasswordException;
import code.vanilson.marketplace.model.Customer;
import code.vanilson.marketplace.model.ROLE;
import code.vanilson.marketplace.model.Token;
import code.vanilson.marketplace.model.User;
import code.vanilson.marketplace.repository.CustomerRepository;
import code.vanilson.marketplace.repository.TokenRepository;
import code.vanilson.marketplace.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RateLimitingService rateLimitingService;
    private final CustomerRepository customerRepository;

    public AuthenticationResponse register(RegisterRequest request) {
        String roleStr = request.getRole() == null || request.getRole().isBlank() ? "USER" : request.getRole();
        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(ROLE.valueOf(roleStr))
                .status("ACTIVE")
                .build();
        var savedUser = repository.save(user);

        // Auto-create a Customer record so /customers/me works immediately after register
        if (customerRepository.findByEmail(request.getEmail()).isEmpty()) {
            Customer customer = new Customer();
            customer.setEmail(request.getEmail());
            customer.setName(request.getEmail().split("@")[0]); // use email prefix as default name
            customer.setAddress("");
            customerRepository.save(customer);
        }

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        if (!rateLimitingService.resolveBucket(request.getEmail()).tryConsume(1)) {
            throw new BadCredentialsException("auth.rate.limit");
        }
        var userOpt = repository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new EmailNotFoundException("auth.email.not.found", "EMAIL_NOT_FOUND");
        }
        var user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IncorrectPasswordException("auth.incorrect.password", "INCORRECT_PASSWORD");
        }
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(Token.TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.repository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}
