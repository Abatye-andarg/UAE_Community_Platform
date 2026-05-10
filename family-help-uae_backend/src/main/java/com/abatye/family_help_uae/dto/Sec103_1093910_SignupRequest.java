package com.abatye.family_help_uae.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for family registration requests.
 */
@Data
public class Sec103_1093910_SignupRequest {
    @NotBlank(message = "Family name is required")
    @Size(max = 100)
    private String familyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String password;

    @Size(max = 255)
    private String address;

    @Size(max = 20)
    private String phone;
}
