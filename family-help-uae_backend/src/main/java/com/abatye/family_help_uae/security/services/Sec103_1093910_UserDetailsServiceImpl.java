package com.abatye.family_help_uae.security.services;

import com.abatye.family_help_uae.model.Sec103_1093910_Family;
import com.abatye.family_help_uae.repository.Sec103_1093910_FamilyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to load user-specific data from the database.
 */
@Service
@RequiredArgsConstructor
public class Sec103_1093910_UserDetailsServiceImpl implements UserDetailsService {

    private final Sec103_1093910_FamilyRepository familyRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Sec103_1093910_Family family = familyRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Family not found with email: " + email));

        return Sec103_1093910_UserDetailsImpl.build(family);
    }
}
