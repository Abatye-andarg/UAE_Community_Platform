package com.abatye.family_help_uae.repository;

import com.abatye.family_help_uae.model.Sec103_1093910_HelpOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Sec103_1093910_HelpOfferRepository extends JpaRepository<Sec103_1093910_HelpOffer, Long> {

    List<Sec103_1093910_HelpOffer> findByFamilyId(Long familyId);

    List<Sec103_1093910_HelpOffer> findByCategoryId(Long categoryId);

    List<Sec103_1093910_HelpOffer> findByStatus(String status);

    List<Sec103_1093910_HelpOffer> findByFamilyIdAndStatus(Long familyId, String status);

    void deleteByFamilyId(Long familyId);
}
