package co.edu.uniquindio.config;

import org.springframework.security.core.GrantedAuthority;

public class SimpleGrantedAuthorityNoPrefix implements GrantedAuthority {
    private final String authority;

    public SimpleGrantedAuthorityNoPrefix(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}