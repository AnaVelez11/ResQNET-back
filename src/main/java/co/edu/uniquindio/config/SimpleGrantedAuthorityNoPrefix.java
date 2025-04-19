package co.edu.uniquindio.config;

import org.springframework.security.core.GrantedAuthority;

public class SimpleGrantedAuthorityNoPrefix implements GrantedAuthority {
    private final String authority;

    /**
     * Implementación personalizada de GrantedAuthority que:
     * - Almacena un rol/permiso ("ADMIN" en lugar de "ROLE_ADMIN")
     * - Elimina el prefijo automático "ROLE_" que Spring Security agrega por defecto
     * - Se usa en la autenticación para comparar roles directamente
     */
    public SimpleGrantedAuthorityNoPrefix(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}