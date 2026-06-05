package se.jensen.leo.demon.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.jensen.leo.demon.config.SecurityConfig;
import se.jensen.leo.demon.controller.UserController;
import se.jensen.leo.demon.repository.UserRepository;
import se.jensen.leo.demon.service.UserService;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class
})
class UserApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;


    @Test
    void createUserShouldBePublic() throws Exception {
        String json = """
                {
                  "fullName": "Test User",
                  "email": "test@example.com",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void getUsersShouldRequireLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void getUsersShouldWorkWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk());
    }
}