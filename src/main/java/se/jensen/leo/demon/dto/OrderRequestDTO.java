package se.jensen.leo.demon.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @Valid
    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequestDTO> items;
}