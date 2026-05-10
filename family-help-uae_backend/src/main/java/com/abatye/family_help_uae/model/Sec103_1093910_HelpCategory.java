package com.abatye.family_help_uae.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "help_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sec103_1093910_HelpCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
}
