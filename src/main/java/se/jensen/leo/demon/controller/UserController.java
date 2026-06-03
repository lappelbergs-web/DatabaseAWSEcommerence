package se.jensen.leo.demon.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import se.jensen.leo.demon.dto.UserRequestDTO;
import se.jensen.leo.demon.dto.UserResponseDTO;
import se.jensen.leo.demon.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponseDTO create(@Valid @RequestBody UserRequestDTO dto) {
        return userService.create(dto);
    }

    @GetMapping
    public List<UserResponseDTO> getAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponseDTO getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    public UserResponseDTO update(
            @PathVariable Long id,
            @RequestBody UserRequestDTO dto
    ) {
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}