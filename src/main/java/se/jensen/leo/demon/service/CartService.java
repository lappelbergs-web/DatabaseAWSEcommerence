package se.jensen.leo.demon.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import se.jensen.leo.demon.dto.CartItemDTO;
import se.jensen.leo.demon.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private static final String CART_SESSION_KEY = "cartItems";

    private final ProductService productService;

    public CartService(ProductService productService) {
        this.productService = productService;
    }

    public List<CartItemDTO> getCartItems(HttpSession session) {
        Object cart = session.getAttribute(CART_SESSION_KEY);

        if (cart == null) {
            List<CartItemDTO> items = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, items);
            return items;
        }

        return (List<CartItemDTO>) cart;
    }

    public void addItem(Long productId, Integer quantity, HttpSession session) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }

        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        List<CartItemDTO> items = getCartItems(session);

        for (CartItemDTO item : items) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        Product product = productService.findById(productId);

        CartItemDTO newItem = CartItemDTO.builder()
                .productId(product.getId())
                .productTitle(product.getTitle())
                .productPrice(product.getPrice())
                .image(product.getImage())
                .quantity(quantity)
                .build();

        items.add(newItem);
    }

    public void removeItem(Long productId, HttpSession session) {
        List<CartItemDTO> items = getCartItems(session);
        items.removeIf(item -> item.getProductId().equals(productId));
    }

    public BigDecimal getTotalPrice(HttpSession session) {
        return getCartItems(session)
                .stream()
                .map(CartItemDTO::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }
}