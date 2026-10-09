package nl.outokumpu.afspraken.security;

import nl.outokumpu.afspraken.enums.GebruikersRol;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class GebruikerPrincipal
        implements UserDetails {

    private final UUID id;
    private final String email;
    private final String wachtwoordHash;
    private final GebruikersRol rol;
    private final boolean actief;

    public GebruikerPrincipal(
            UUID id,
            String email,
            String wachtwoordHash,
            GebruikersRol rol,
            boolean actief
    ) {
        this.id = id;
        this.email = email;
        this.wachtwoordHash = wachtwoordHash;
        this.rol = rol;
        this.actief = actief;
    }

    public UUID getId() {
        return id;
    }

    public GebruikersRol getRol() {
        return rol;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + rol.name()
                )
        );
    }

    @Override
    public String getPassword() {
        return wachtwoordHash;
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
        return actief;
    }
}