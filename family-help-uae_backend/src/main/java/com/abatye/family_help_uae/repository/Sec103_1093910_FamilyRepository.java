package com.abatye.family_help_uae.repository;

import com.abatye.family_help_uae.model.Sec103_1093910_Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Sec103_1093910_FamilyRepository extends JpaRepository<Sec103_1093910_Family, Long> {

    Optional<Sec103_1093910_Family> findByEmail(String email);

    boolean existsByEmail(String email);
}
