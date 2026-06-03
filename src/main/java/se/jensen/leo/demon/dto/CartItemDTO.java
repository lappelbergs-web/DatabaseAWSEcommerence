package se.jensen.leo.demon.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {

    private Long productId;
    private String productTitle;
    private BigDecimal productPrice;
    private String image;
    private Integer quantity;

    public BigDecimal getLineTotal() {
        return productPrice.multiply(BigDecimal.valueOf(quantity));
    }
}