package se.jensen.leo.demon.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    private Long id;
    private String title;
    private BigDecimal price;
    private String description;
    private String category;
    private String image;
}