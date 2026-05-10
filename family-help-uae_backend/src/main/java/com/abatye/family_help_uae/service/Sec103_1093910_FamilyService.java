package com.abatye.family_help_uae.service;

import com.abatye.family_help_uae.model.Sec103_1093910_Family;

import java.util.List;
import java.util.Optional;

public interface Sec103_1093910_FamilyService {

    Sec103_1093910_Family save(Sec103_1093910_Family family);

    Optional<Sec103_1093910_Family> findById(Long id);

    Optional<Sec103_1093910_Family> findByEmail(String email);

    List<Sec103_1093910_Family> findAll();

    Sec103_1093910_Family update(Long id, Sec103_1093910_Family family);

    void deleteById(Long id);

    boolean existsByEmail(String email);
}
