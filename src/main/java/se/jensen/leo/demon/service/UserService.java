package se.jensen.leo.demon.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.jensen.leo.demon.dto.UserRequestDTO;
import se.jensen.leo.demon.dto.UserResponseDTO;
import se.jensen.leo.demon.model.User;
import se.jensen.leo.demon.repository.UserRepository;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        return toResponseDTO(user);
    }

    public UserResponseDTO create(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .role("ROLE_USER")
                .build();

        User saved = userRepository.save(user);

        return toResponseDTO(saved);
    }

    public UserResponseDTO update(Long id, UserRequestDTO dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (dto.getEmail() != null && !dto.getEmail().equals(existing.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }

            existing.setEmail(dto.getEmail());
        }

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            existing.setFullName(dto.getFullName());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User saved = userRepository.save(existing);

        return toResponseDTO(saved);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }

        userRepository.deleteById(id);
    }

    private UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}