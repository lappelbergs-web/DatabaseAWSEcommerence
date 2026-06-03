package se.jensen.leo.demon.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import se.jensen.leo.demon.dto.OrderRequestDTO;
import se.jensen.leo.demon.dto.OrderResponseDTO;
import se.jensen.leo.demon.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponseDTO create(@Valid @RequestBody OrderRequestDTO dto) {
        return orderService.createOrder(dto);
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponseDTO> getByUser(@PathVariable Long userId) {
        return orderService.findByUserId(userId);
    }
}