package se.jensen.leo.demon.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {

    private Long orderId;
    private Long userId;
    private List<OrderItemResponseDTO> items;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;
}