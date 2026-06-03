package se.jensen.leo.demon.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.jensen.leo.demon.exception.GlobalExceptionHandler;
import se.jensen.leo.demon.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void createShouldReturnBadRequestWhenUserInputIsInvalid() throws Exception {
        String invalidJson = """
                {
                  "fullName": "",
                  "email": "bad-email",
                  "password": "123"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fullName").value("Full name is required"))
                .andExpect(jsonPath("$.email").value("Email must be valid"))
                .andExpect(jsonPath("$.password").value("Password must be at least 6 characters"));
    }
}