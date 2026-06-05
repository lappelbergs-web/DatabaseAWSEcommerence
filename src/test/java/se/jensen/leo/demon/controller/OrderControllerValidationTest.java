package se.jensen.leo.demon.controller;

import se.jensen.leo.demon.dto.OrderResponseDTO;
import se.jensen.leo.demon.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.jensen.leo.demon.exception.GlobalExceptionHandler;
import se.jensen.leo.demon.repository.UserRepository;
import se.jensen.leo.demon.service.OrderService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockitoBean
    private UserRepository userRepository;

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

    @Test
    void getMyOrdersShouldReturnOrdersForAuthenticatedUser() throws Exception {
        User user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .password("encoded-password")
                .fullName("Test User")
                .role("ROLE_USER")
                .build();

        OrderResponseDTO order = OrderResponseDTO.builder()
                .orderId(100L)
                .userId(1L)
                .items(List.of())
                .totalPrice(new BigDecimal("0.00"))
                .status("CREATED")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderService.findByUserId(1L)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/me")
                        .principal(new UsernamePasswordAuthenticationToken(
                                "test@example.com",
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(100))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }
}