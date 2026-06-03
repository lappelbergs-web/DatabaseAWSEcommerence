package se.jensen.leo.demon.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.jensen.leo.demon.dto.OrderItemRequestDTO;
import se.jensen.leo.demon.dto.OrderResponseDTO;
import se.jensen.leo.demon.dto.OrderRequestDTO;
import se.jensen.leo.demon.model.Order;
import se.jensen.leo.demon.model.Product;
import se.jensen.leo.demon.model.User;
import se.jensen.leo.demon.repository.OrderRepository;
import se.jensen.leo.demon.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductService productService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, userRepository, productService);
    }

    @Test
    void createOrderShouldSaveOrderWithItemsAndCorrectTotalPrice() {
        User user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .password("encoded-password")
                .fullName("Test User")
                .role("ROLE_USER")
                .build();

        Product productOne = Product.builder()
                .id(1L)
                .title("Product One")
                .price(new BigDecimal("10.00"))
                .description("First product")
                .category("test")
                .image("image-one.jpg")
                .build();

        Product productTwo = Product.builder()
                .id(2L)
                .title("Product Two")
                .price(new BigDecimal("5.50"))
                .description("Second product")
                .category("test")
                .image("image-two.jpg")
                .build();

        OrderRequestDTO request = OrderRequestDTO.builder()
                .userId(1L)
                .items(List.of(
                        OrderItemRequestDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .build(),
                        OrderItemRequestDTO.builder()
                                .productId(2L)
                                .quantity(1)
                                .build()
                ))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productService.findById(1L)).thenReturn(productOne);
        when(productService.findById(2L)).thenReturn(productTwo);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setOrderId(100L);

            for (int i = 0; i < order.getItems().size(); i++) {
                order.getItems().get(i).setOrderItemId((long) i + 1);
            }

            return order;
        });

        OrderResponseDTO response = orderService.createOrder(request);

        assertEquals(100L, response.getOrderId());
        assertEquals(1L, response.getUserId());
        assertEquals("CREATED", response.getStatus());
        assertEquals(new BigDecimal("25.50"), response.getTotalPrice());
        assertEquals(2, response.getItems().size());

        assertEquals(1L, response.getItems().get(0).getProductId());
        assertEquals("Product One", response.getItems().get(0).getProductTitle());
        assertEquals(new BigDecimal("10.00"), response.getItems().get(0).getProductPrice());
        assertEquals(2, response.getItems().get(0).getQuantity());

        assertEquals(2L, response.getItems().get(1).getProductId());
        assertEquals("Product Two", response.getItems().get(1).getProductTitle());
        assertEquals(new BigDecimal("5.50"), response.getItems().get(1).getProductPrice());
        assertEquals(1, response.getItems().get(1).getQuantity());

        verify(orderRepository).save(argThat(order ->
                order.getUser().equals(user) &&
                        order.getItems().size() == 2 &&
                        order.getTotalPrice().equals(new BigDecimal("25.50")) &&
                        order.getStatus().equals("CREATED") &&
                        order.getCreatedAt() != null
        ));
    }

    @Test
    void createOrderShouldThrowWhenUserDoesNotExist() {
        OrderRequestDTO request = OrderRequestDTO.builder()
                .userId(99L)
                .items(List.of(
                        OrderItemRequestDTO.builder()
                                .productId(1L)
                                .quantity(1)
                                .build()
                ))
                .build();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals("User not found: 99", exception.getMessage());

        verify(productService, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void findByUserIdShouldReturnOrderResponses() {
        User user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .password("encoded-password")
                .fullName("Test User")
                .role("ROLE_USER")
                .build();

        Order order = Order.builder()
                .orderId(100L)
                .user(user)
                .items(List.of())
                .totalPrice(new BigDecimal("0.00"))
                .status("CREATED")
                .build();

        when(orderRepository.findByUserUserId(1L)).thenReturn(List.of(order));

        List<OrderResponseDTO> responses = orderService.findByUserId(1L);

        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getOrderId());
        assertEquals(1L, responses.get(0).getUserId());
        assertEquals(new BigDecimal("0.00"), responses.get(0).getTotalPrice());
        assertEquals("CREATED", responses.get(0).getStatus());
    }
}