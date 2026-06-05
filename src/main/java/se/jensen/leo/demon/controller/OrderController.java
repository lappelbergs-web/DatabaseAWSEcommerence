package se.jensen.leo.demon.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import se.jensen.leo.demon.dto.OrderRequestDTO;
import se.jensen.leo.demon.dto.OrderResponseDTO;
import se.jensen.leo.demon.model.User;
import se.jensen.leo.demon.repository.UserRepository;
import se.jensen.leo.demon.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public OrderResponseDTO create(@Valid @RequestBody OrderRequestDTO dto) {
        return orderService.createOrder(dto);
    }

    @GetMapping("/me")
    public List<OrderResponseDTO> getMyOrders(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found: " + authentication.getName()));

        return orderService.findByUserId(user.getUserId());
    }
}