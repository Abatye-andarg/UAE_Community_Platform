package com.abatye.family_help_uae.service;

import com.abatye.family_help_uae.model.Sec103_1093910_HelpCategory;

import java.util.List;
import java.util.Optional;

public interface Sec103_1093910_HelpCategoryService {

    Sec103_1093910_HelpCategory save(Sec103_1093910_HelpCategory category);

    Optional<Sec103_1093910_HelpCategory> findById(Long id);

    Optional<Sec103_1093910_HelpCategory> findByName(String name);

    List<Sec103_1093910_HelpCategory> findAll();

    Sec103_1093910_HelpCategory update(Long id, Sec103_1093910_HelpCategory category);

    void deleteById(Long id);

    boolean existsByName(String name);
}
