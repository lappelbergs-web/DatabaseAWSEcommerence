package se.jensen.leo.demon.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import se.jensen.leo.demon.model.Product;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://fakestoreapi.com")
            .build();

    public List<Product> findAll() {
        Product[] products = restClient.get()
                .uri("/products")
                .retrieve()
                .body(Product[].class);

        return products == null ? List.of() : Arrays.asList(products);
    }

    public Product findById(Long id) {
        return restClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .body(Product.class);
    }
}