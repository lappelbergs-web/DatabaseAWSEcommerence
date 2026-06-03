package se.jensen.leo.demon.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import se.jensen.leo.demon.repository.UserRepository;
import se.jensen.leo.demon.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    @Bean
    public CustomUserDetailsService customUserDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/products",
                                "/products/**",
                                "/css/**",
                                "/login",
                                "/register",
                                "/api/products",
                                "/api/products/**",
                                "/api/users",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/products", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .build();
    }
}