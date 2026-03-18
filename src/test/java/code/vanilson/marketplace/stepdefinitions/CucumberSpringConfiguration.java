package code.vanilson.marketplace.stepdefinitions;

/**
 * CucumberSpringConfiguration
 *
 * @author vamuhong
 * @version 1.0
 * @since 2024-06-14
 */

import code.vanilson.marketplace.config.JwtAuthenticationFilter;
import code.vanilson.marketplace.config.JwtService;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@CucumberContextConfiguration
@TestPropertySource(properties = {"spring.config.location=classpath:config/application-test.yml"})
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class CucumberSpringConfiguration {
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private AuthenticationProvider authenticationProvider;
    @MockBean
    private LogoutHandler logoutHandler;
}