package org.example.learnlink.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.learnlink.modules.user.entity.AcademicLevel;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "firstName is required")
    @Size(min = 3, max = 50, message = "firstName must be between 3 and 50 characters")
    private String firstName;

    @NotBlank(message = "lastName is required")
    @Size(min = 3, max = 50, message = "lastName must be between 3 and 50 characters")
    private String lastName;

    @NotBlank(message = "bio is required")
    @Size(min = 3, max = 50, message = "bio must be between 20 and 500 characters")
    private String bio;


    private List<Long> studentSubjectId;

    private AcademicLevel academicLevel;

}
