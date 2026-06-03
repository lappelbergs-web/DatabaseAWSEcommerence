package se.jensen.leo.demon.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.jensen.leo.demon.dto.UserRequestDTO;
import se.jensen.leo.demon.dto.UserResponseDTO;
import se.jensen.leo.demon.model.User;
import se.jensen.leo.demon.repository.UserRepository;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createShouldSaveUserWithEncodedPasswordAndUserRole() {
        UserRequestDTO request = UserRequestDTO.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .build();

        Mockito.when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        Mockito.when(userRepository.save(ArgumentMatchers.any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(1L);
            return user;
        });

        UserResponseDTO response = userService.create(request);

        Assertions.assertEquals(1L, response.getUserId());
        Assertions.assertEquals("test@example.com", response.getEmail());
        Assertions.assertEquals("Test User", response.getFullName());
        Assertions.assertEquals("ROLE_USER", response.getRole());

        Mockito.verify(passwordEncoder).encode("password123");
        Mockito.verify(userRepository).save(ArgumentMatchers.argThat(user ->
                user.getEmail().equals("test@example.com") &&
                        user.getFullName().equals("Test User") &&
                        user.getPassword().equals("encoded-password") &&
                        user.getRole().equals("ROLE_USER")
        ));
    }

    @Test
    void createShouldThrowWhenEmailAlreadyExists() {
        UserRequestDTO request = UserRequestDTO.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .build();

        Mockito.when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.create(request)
        );

        Assertions.assertEquals("Email already in use: test@example.com", exception.getMessage());

        Mockito.verify(passwordEncoder, Mockito.never()).encode(ArgumentMatchers.any());
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any());
    }

    @Test
    void findByIdShouldReturnUserResponseDTO() {
        User user = User.builder()
                .userId(1L)
                .fullName("Test User")
                .email("test@example.com")
                .password("encoded-password")
                .role("ROLE_USER")
                .build();

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.findById(1L);

        Assertions.assertEquals(1L, response.getUserId());
        Assertions.assertEquals("Test User", response.getFullName());
        Assertions.assertEquals("test@example.com", response.getEmail());
        Assertions.assertEquals("ROLE_USER", response.getRole());
    }
}