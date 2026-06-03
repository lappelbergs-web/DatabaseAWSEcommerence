package se.jensen.leo.demon.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDTO {

    private Long orderItemId;
    private Long productId;
    private String productTitle;
    private BigDecimal productPrice;
    private Integer quantity;
}