package se.jensen.leo.demon.controller;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import se.jensen.leo.demon.dto.UserRequestDTO;
import se.jensen.leo.demon.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import se.jensen.leo.demon.dto.CartItemDTO;
import se.jensen.leo.demon.dto.OrderItemRequestDTO;
import se.jensen.leo.demon.dto.OrderRequestDTO;
import se.jensen.leo.demon.dto.OrderResponseDTO;
import se.jensen.leo.demon.model.User;
import se.jensen.leo.demon.repository.UserRepository;
import se.jensen.leo.demon.service.CartService;
import se.jensen.leo.demon.service.OrderService;
import se.jensen.leo.demon.service.ProductService;

import java.util.List;

@Controller
public class PageController {

    private final ProductService productService;
    private final OrderService orderService;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final UserService userService;

    public PageController(
            ProductService productService,
            OrderService orderService,
            CartService cartService,
            UserRepository userRepository,
            UserService userService
    ) {
        this.productService = productService;
        this.orderService = orderService;
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new UserRequestDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") UserRequestDTO dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.create(dto);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            return "register";
        }

        return "redirect:/login";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.findAll());
        return "products";
    }

    @GetMapping("/products/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "product-details";
    }

    @PostMapping("/cart/items")
    public String addToCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            HttpSession session
    ) {
        cartService.addItem(productId, quantity, session);
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {
        model.addAttribute("items", cartService.getCartItems(session));
        model.addAttribute("totalPrice", cartService.getTotalPrice(session));
        return "cart";
    }

    @PostMapping("/cart/items/{productId}/remove")
    public String removeFromCart(@PathVariable Long productId, HttpSession session) {
        cartService.removeItem(productId, session);
        return "redirect:/cart";
    }

    @GetMapping("/orders")
    public String orderHistory(Authentication authentication, Model model) {
        User user = getLoggedInUser(authentication);

        model.addAttribute("user", user);
        model.addAttribute("orders", orderService.findByUserId(user.getUserId()));

        return "orders";
    }

    @PostMapping("/cart/checkout")
    public String checkout(
            Authentication authentication,
            HttpSession session,
            Model model
    ) {
        User user = getLoggedInUser(authentication);

        List<CartItemDTO> cartItems = cartService.getCartItems(session);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        List<OrderItemRequestDTO> orderItems = cartItems.stream()
                .map(item -> OrderItemRequestDTO.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        OrderRequestDTO request = OrderRequestDTO.builder()
                .userId(user.getUserId())
                .items(orderItems)
                .build();

        OrderResponseDTO order = orderService.createOrder(request);
        cartService.clearCart(session);

        model.addAttribute("order", order);

        return "order-confirmation";
    }

    private User getLoggedInUser(Authentication authentication) {
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found: " + email));
    }
}