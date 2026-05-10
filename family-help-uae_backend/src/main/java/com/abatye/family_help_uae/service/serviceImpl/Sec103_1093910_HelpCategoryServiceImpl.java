package com.abatye.family_help_uae.service.serviceImpl;

import com.abatye.family_help_uae.config.Sec103_1093910_CacheConfig;
import com.abatye.family_help_uae.model.Sec103_1093910_HelpCategory;
import com.abatye.family_help_uae.repository.Sec103_1093910_HelpCategoryRepository;
import com.abatye.family_help_uae.service.Sec103_1093910_HelpCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for help category management.
 *
 * <p>Categories are reference/lookup data (e.g., Tutoring, Transport, Childcare).
 * Category names must be unique across the platform.</p>
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
public class Sec103_1093910_HelpCategoryServiceImpl implements Sec103_1093910_HelpCategoryService {

    private final Sec103_1093910_HelpCategoryRepository helpCategoryRepository;

    @Override
    @CacheEvict(value = Sec103_1093910_CacheConfig.CACHE_HELP_CATEGORIES, allEntries = true)
    public Sec103_1093910_HelpCategory save(Sec103_1093910_HelpCategory category) {
        if (helpCategoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException(
                    "Category '" + category.getName() + "' already exists.");
        }
        return helpCategoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sec103_1093910_HelpCategory> findById(Long id) {
        return helpCategoryRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sec103_1093910_HelpCategory> findByName(String name) {
        return helpCategoryRepository.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = Sec103_1093910_CacheConfig.CACHE_HELP_CATEGORIES, key = "'all'")
    public List<Sec103_1093910_HelpCategory> findAll() {
        return helpCategoryRepository.findAll();
    }

    @Override
    @CacheEvict(value = Sec103_1093910_CacheConfig.CACHE_HELP_CATEGORIES, allEntries = true)
    public Sec103_1093910_HelpCategory update(Long id, Sec103_1093910_HelpCategory updated) {
        Sec103_1093910_HelpCategory existing = helpCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));

        if (!existing.getName().equalsIgnoreCase(updated.getName())
                && helpCategoryRepository.existsByName(updated.getName())) {
            throw new IllegalArgumentException(
                    "Category name '" + updated.getName() + "' is already in use.");
        }

        existing.setName(updated.getName());
        return helpCategoryRepository.save(existing);
    }

    @Override
    @CacheEvict(value = Sec103_1093910_CacheConfig.CACHE_HELP_CATEGORIES, allEntries = true)
    public void deleteById(Long id) {
        if (!helpCategoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found with id: " + id);
        }
        helpCategoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return helpCategoryRepository.existsByName(name);
    }
}
