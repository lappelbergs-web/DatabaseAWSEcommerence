package se.jensen.leo.demon.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.jensen.leo.demon.exception.GlobalExceptionHandler;
import se.jensen.leo.demon.service.OrderService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class OrderControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createShouldReturnBadRequestWhenQuantityIsInvalid() throws Exception {
        String invalidJson = """
                {
                  "userId": 1,
                  "items": [
                    {
                      "productId": 1,
                      "quantity": 0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.['items[0].quantity']").value("Quantity must be at least 1"));
    }

    @Test
    void createShouldReturnBadRequestWhenItemsAreMissing() throws Exception {
        String invalidJson = """
                {
                  "userId": 1,
                  "items": []
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.items").value("Order must contain at least one item"));
    }

    @Test
    void createShouldReturnBadRequestWhenUserIdIsMissing() throws Exception {
        String invalidJson = """
                {
                  "items": [
                    {
                      "productId": 1,
                      "quantity": 1
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.userId").value("User ID is required"));
    }
}