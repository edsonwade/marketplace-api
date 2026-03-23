package code.vanilson.marketplace.controller.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    // role is intentionally removed — all self-registrations are USER.
    // Admin accounts are created via Flyway seed only.
}
