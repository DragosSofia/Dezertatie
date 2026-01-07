package org.example.auth.dtos;

import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterDto {
    String email;
    String emailUpb;
    String password;
}
