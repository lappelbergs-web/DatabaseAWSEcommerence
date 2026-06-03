package se.jensen.leo.demon.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private Long userId;
    private String email;
    private String fullName;
    private String role;
}