package com.abatye.family_help_uae.security.services;

import com.abatye.family_help_uae.model.Sec103_1093910_Family;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of UserDetails to hold family security information.
 */
@AllArgsConstructor
@Getter
public class Sec103_1093910_UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String familyName;
    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public static Sec103_1093910_UserDetailsImpl build(Sec103_1093910_Family family) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(family.getRole())
        );

        return new Sec103_1093910_UserDetailsImpl(
                family.getId(),
                family.getFamilyName(),
                family.getEmail(),
                family.getPasswordHash(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sec103_1093910_UserDetailsImpl user = (Sec103_1093910_UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
