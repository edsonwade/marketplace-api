package code.vanilson.marketplace.controller.auth;

import code.vanilson.marketplace.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationService service;

    @InjectMocks
    private AuthenticationController controller;

    private ObjectMapper objectMapper;
    private AuthenticationResponse authResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        authResponse = AuthenticationResponse.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-456")
                .build();
    }

    // ── POST /register ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Register - returns 200 with tokens")
    void testRegisterReturnsOk() throws Exception {
        // role field no longer exists in RegisterRequest — self-registration always USER
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(service.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token-123"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token-456"));

        verify(service, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Register - service is called exactly once")
    void testRegisterCallsServiceOnce() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("newpassword")
                .build();

        AuthenticationResponse newResponse = AuthenticationResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        when(service.register(any(RegisterRequest.class))).thenReturn(newResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new-access-token"))
                .andExpect(jsonPath("$.refresh_token").value("new-refresh-token"));

        verify(service, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Register - extra fields in JSON body are ignored (no role escalation)")
    void testRegisterIgnoresExtraRoleField() throws Exception {
        // Even if a client sends a role field in the JSON body, it is ignored
        // because RegisterRequest no longer has that field
        String body = "{\"email\":\"hacker@example.com\",\"password\":\"pass123\",\"role\":\"ADMIN\"}";

        when(service.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // Service is still called — but the role field was silently ignored by Jackson
        verify(service, times(1)).register(any(RegisterRequest.class));
    }

    // ── POST /login ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Login - returns 200 with tokens")
    void testLoginReturnsOk() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(service.authenticate(any(AuthenticationRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token-123"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token-456"));

        verify(service, times(1)).authenticate(any(AuthenticationRequest.class));
    }

    @Test
    @DisplayName("Login - service is called exactly once")
    void testLoginCallsServiceOnce() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(service.authenticate(any(AuthenticationRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(service, times(1)).authenticate(any(AuthenticationRequest.class));
    }
}
