package se.jensen.leo.demon.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.jensen.leo.demon.dto.OrderItemResponseDTO;
import se.jensen.leo.demon.dto.OrderRequestDTO;
import se.jensen.leo.demon.dto.OrderResponseDTO;
import se.jensen.leo.demon.model.Order;
import se.jensen.leo.demon.model.OrderItem;
import se.jensen.leo.demon.model.Product;
import se.jensen.leo.demon.model.User;
import se.jensen.leo.demon.repository.OrderRepository;
import se.jensen.leo.demon.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            ProductService productService
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.getUserId()));

        Order order = Order.builder()
                .user(user)
                .totalPrice(BigDecimal.ZERO)
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        List<OrderItem> items = dto.getItems()
                .stream()
                .map(itemDto -> {
                    Product product = productService.findById(itemDto.getProductId());

                    return OrderItem.builder()
                            .order(order)
                            .productId(product.getId())
                            .productTitle(product.getTitle())
                            .productPrice(product.getPrice())
                            .quantity(itemDto.getQuantity())
                            .build();
                })
                .toList();

        BigDecimal totalPrice = items.stream()
                .map(item -> item.getProductPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setItems(items);
        order.setTotalPrice(totalPrice);

        Order saved = orderRepository.save(order);

        return toResponseDTO(saved);
    }

    public List<OrderResponseDTO> findByUserId(Long userId) {
        return orderRepository.findByUserUserId(userId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private OrderResponseDTO toResponseDTO(Order order) {
        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .items(order.getItems()
                        .stream()
                        .map(this::toItemResponseDTO)
                        .toList())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderItemResponseDTO toItemResponseDTO(OrderItem item) {
        return OrderItemResponseDTO.builder()
                .orderItemId(item.getOrderItemId())
                .productId(item.getProductId())
                .productTitle(item.getProductTitle())
                .productPrice(item.getProductPrice())
                .quantity(item.getQuantity())
                .build();
    }
}