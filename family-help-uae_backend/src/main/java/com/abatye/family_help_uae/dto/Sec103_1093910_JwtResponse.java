package com.abatye.family_help_uae.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for authentication responses containing the JWT.
 */
@Data
@AllArgsConstructor
public class Sec103_1093910_JwtResponse {
    private String token;
    private Long id;
    private String email;
    private String familyName;
}
