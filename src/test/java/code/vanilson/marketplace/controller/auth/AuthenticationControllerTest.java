package code.vanilson.marketplace.controller.auth;

import code.vanilson.marketplace.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

    @Test
    void testRegisterReturnsOk() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role("USER")
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
    void testRegisterWithAdminRole() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("admin@example.com")
                .password("admin123")
                .role("ADMIN")
                .build();

        when(service.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(service, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void testAuthenticateReturnsOk() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(service.authenticate(any(AuthenticationRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token-123"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token-456"));

        verify(service, times(1)).authenticate(any(AuthenticationRequest.class));
    }

    @Test
    void testRegisterWithManagerRole() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("manager@example.com")
                .password("manager123")
                .role("MANAGER")
                .build();

        when(service.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(service, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void testRegisterReturnsTokens() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("newpassword")
                .role("USER")
                .build();

        AuthenticationResponse newUserResponse = AuthenticationResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        when(service.register(any(RegisterRequest.class))).thenReturn(newUserResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new-access-token"))
                .andExpect(jsonPath("$.refresh_token").value("new-refresh-token"));

        verify(service, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void testAuthenticateServiceCalledOnce() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(service.authenticate(any(AuthenticationRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(service, times(1)).authenticate(any(AuthenticationRequest.class));
    }
}
