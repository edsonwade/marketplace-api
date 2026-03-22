package code.vanilson.marketplace.service;

import code.vanilson.marketplace.config.JwtService;
import code.vanilson.marketplace.controller.auth.AuthenticationRequest;
import code.vanilson.marketplace.controller.auth.AuthenticationResponse;
import code.vanilson.marketplace.controller.auth.RegisterRequest;
import code.vanilson.marketplace.model.Customer;
import code.vanilson.marketplace.model.ROLE;
import code.vanilson.marketplace.model.Token;
import code.vanilson.marketplace.model.User;
import code.vanilson.marketplace.repository.CustomerRepository;
import code.vanilson.marketplace.repository.TokenRepository;
import code.vanilson.marketplace.repository.UserRepository;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private UserRepository      repository;
    @Mock private TokenRepository     tokenRepository;
    @Mock private PasswordEncoder     passwordEncoder;
    @Mock private JwtService          jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RateLimitingService rateLimitingService;
    @Mock private CustomerRepository  customerRepository;

    // Construct manually — constructor now includes CustomerRepository
    private AuthenticationService authenticationService;

    private User testUser;
    private RegisterRequest registerRequest;
    private AuthenticationRequest authRequest;
    private Bucket mockBucket;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                repository, tokenRepository, passwordEncoder,
                jwtService, rateLimitingService, customerRepository);

        testUser = User.builder()
                .id(1)
                .email("test@example.com")
                .password("encodedPassword")
                .role(ROLE.USER)
                .status("ACTIVE")
                .build();

        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role("USER")
                .build();

        authRequest = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        mockBucket = mock(Bucket.class);
    }

    @Test
    void testRegisterCreatesUserAndReturnsTokens() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(repository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(tokenRepository.save(any(Token.class))).thenReturn(new Token());
        // No customer exists yet → findByEmail returns empty → should save new Customer
        when(customerRepository.findByEmail(anyString())).thenReturn(java.util.Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(new Customer());

        AuthenticationResponse response = authenticationService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");

        verify(passwordEncoder, times(1)).encode("password123");
        verify(repository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(any(User.class));
        verify(jwtService, times(1)).generateRefreshToken(any(User.class));
        verify(tokenRepository, times(1)).save(any(Token.class));
        // Verify Customer was created
        verify(customerRepository, times(1)).findByEmail("test@example.com");
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testRegisterDoesNotCreateDuplicateCustomer() {
        Customer existing = new Customer(1L, "existing", "test@example.com", "");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(repository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(tokenRepository.save(any(Token.class))).thenReturn(new Token());
        // Customer already exists → findByEmail returns it → should NOT save again
        when(customerRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(existing));

        authenticationService.register(registerRequest);

        verify(customerRepository, times(1)).findByEmail("test@example.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void testRegisterEncodesPasswordWithRole() {
        RegisterRequest adminRequest = RegisterRequest.builder()
                .email("admin@example.com")
                .password("admin123")
                .role("ADMIN")
                .build();

        User adminUser = User.builder()
                .id(2)
                .email("admin@example.com")
                .password("encodedAdminPassword")
                .role(ROLE.ADMIN)
                .status("ACTIVE")
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedAdminPassword");
        when(repository.save(any(User.class))).thenReturn(adminUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("admin-access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("admin-refresh-token");
        when(tokenRepository.save(any(Token.class))).thenReturn(new Token());
        when(customerRepository.findByEmail(anyString())).thenReturn(java.util.Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(new Customer());

        AuthenticationResponse response = authenticationService.register(adminRequest);

        assertThat(response).isNotNull();
        verify(passwordEncoder, times(1)).encode("admin123");
        verify(repository, times(1)).save(argThat(user -> 
            user.getRole() == ROLE.ADMIN && "ACTIVE".equals(user.getStatus())
        ));
    }

    @Test
    void testAuthenticateSuccess() {
        when(rateLimitingService.resolveBucket(anyString())).thenReturn(mockBucket);
        when(mockBucket.tryConsume(1)).thenReturn(true);
        when(repository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(tokenRepository.findAllValidTokenByUser(any())).thenReturn(Collections.emptyList());
        when(tokenRepository.save(any(Token.class))).thenReturn(new Token());

        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");

        verify(rateLimitingService, times(1)).resolveBucket("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(repository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testAuthenticateRateLimited() {
        when(rateLimitingService.resolveBucket(anyString())).thenReturn(mockBucket);
        when(mockBucket.tryConsume(1)).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.authenticate(authRequest))
                .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class)
                .hasMessageContaining("auth.rate.limit");
    }

    @Test
    void testChangePasswordSuccess() {
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword456")).thenReturn("encodedNewPassword");
        when(repository.save(any(User.class))).thenReturn(testUser);

        authenticationService.changePassword("test@example.com", "password123", "newPassword456");

        verify(repository).save(argThat(u -> u.getPassword().equals("encodedNewPassword")));
    }

    @Test
    void testChangePasswordThrowsWhenCurrentPasswordWrong() {
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.changePassword("test@example.com", "wrongPassword", "newPass"))
                .isInstanceOf(code.vanilson.marketplace.exception.BadRequestException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(repository, never()).save(any());
    }

    @Test
    void testChangePasswordThrowsWhenUserNotFound() {
        when(repository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.changePassword("unknown@example.com", "pass", "newPass"))
                .isInstanceOf(code.vanilson.marketplace.exception.ObjectWithIdNotFound.class);
    }

    @Test
    void testAuthenticateWithManagerRole() {
        RegisterRequest managerRequest = RegisterRequest.builder()
                .email("manager@example.com")
                .password("manager123")
                .role("MANAGER")
                .build();

        User managerUser = User.builder()
                .id(3)
                .email("manager@example.com")
                .password("encodedPassword")
                .role(ROLE.MANAGER)
                .status("ACTIVE")
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(repository.save(any(User.class))).thenReturn(managerUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("manager-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("manager-refresh");
        when(tokenRepository.save(any(Token.class))).thenReturn(new Token());
        when(customerRepository.findByEmail(anyString())).thenReturn(java.util.Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(new Customer());

        AuthenticationResponse response = authenticationService.register(managerRequest);

        assertThat(response).isNotNull();
        verify(repository, times(1)).save(argThat(user -> user.getRole() == ROLE.MANAGER));
    }
}
