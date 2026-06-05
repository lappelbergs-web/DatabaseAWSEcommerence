package se.jensen.leo.demon.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.jensen.leo.demon.controller.PageController;
import se.jensen.leo.demon.repository.UserRepository;
import se.jensen.leo.demon.service.CartService;
import se.jensen.leo.demon.service.OrderService;
import se.jensen.leo.demon.service.ProductService;
import se.jensen.leo.demon.service.UserService;
import se.jensen.leo.demon.model.Product;



import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PageController.class)
@AutoConfigureMockMvc
@Import(SecurityAccessTest.TestSecurityConfig.class)
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class
})
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void productsPageShouldBePublic() throws Exception {
        when(productService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"));
    }

    @Test
    void loginPageShouldBePublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void registerPageShouldBePublic() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void cartPageShouldRedirectToLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void ordersPageShouldRedirectToLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void cartPageShouldBeAccessibleWhenAuthenticated() throws Exception {
        when(cartService.getCartItems(any())).thenReturn(List.of());
        when(cartService.getTotalPrice(any())).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/cart")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));
    }

    @Test
    void productDetailsShouldShowLoginToAddToCartWhenAnonymous() throws Exception {
        Product product = Product.builder()
                .id(1L)
                .title("Test Product")
                .price(new BigDecimal("10.00"))
                .description("Test description")
                .category("test")
                .image("image.jpg")
                .build();

        when(productService.findById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-details"))
                .andExpect(content().string(containsString("Login to Add to Cart")));
    }

    @Test
    void productDetailsShouldShowAddToCartWhenAuthenticated() throws Exception {
        Product product = Product.builder()
                .id(1L)
                .title("Test Product")
                .price(new BigDecimal("10.00"))
                .description("Test description")
                .category("test")
                .image("image.jpg")
                .build();

        when(productService.findById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("product-details"))
                .andExpect(content().string(containsString("Add to Cart")));
    }

    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    "/",
                                    "/products",
                                    "/products/**",
                                    "/css/**",
                                    "/login",
                                    "/register"
                            ).permitAll()
                            .anyRequest().authenticated()
                    )
                    .formLogin(form -> form
                            .loginPage("/login")
                            .permitAll()
                    )
                    .build();
        }
    }
}