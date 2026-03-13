package com.ferrumlog.security;

import com.ferrumlog.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Implementación personalizada de UserDetails para Spring Security.
 * Envuelve nuestra entidad User y proporciona la información de seguridad.
 */
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Por ahora todos los usuarios tienen el rol USER
        // En el futuro se puede extender para roles específicos (ADMIN, TRAINER, etc.)
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
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

    /**
     * Obtiene el ID del usuario autenticado.
     * Útil para operaciones que requieren asociar datos al usuario actual.
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * Obtiene la entidad User completa.
     */
    public User getUser() {
        return user;
    }
}
