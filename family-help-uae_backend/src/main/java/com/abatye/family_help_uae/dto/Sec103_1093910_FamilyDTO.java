package com.abatye.family_help_uae.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Safe DTO for family information (no password hash).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sec103_1093910_FamilyDTO {
    private Long id;
    private String familyName;
    private String email;
    private String address;
    private String phone;
    private String role;
}
