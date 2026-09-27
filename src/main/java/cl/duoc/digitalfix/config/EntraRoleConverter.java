package cl.duoc.digitalfix.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class EntraRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        addRoles(authorities, jwt.getClaimAsStringList("roles"));
        addClaim(authorities, jwt.getClaimAsStringList("scp"), "SCOPE_");
        return authorities;
    }

    private void addRoles(List<GrantedAuthority> authorities, List<String> values) {
        if (values == null) return;
        values.stream()
                .map(this::normalizeRole)
                .filter(role -> role != null)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .forEach(authorities::add);
    }

    private String normalizeRole(String value) {
        String normalized = value == null ? "" : value.trim().replaceFirst("(?i)^role[_-]?", "");
        normalized = normalized.replaceAll("[\\s-]+", "_").toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ADMIN", "ADMINISTRADOR" -> "Admin";
            case "SUPERVISOR", "OPERADOR" -> "Supervisor";
            case "CLIENTE", "CUSTOMER" -> "Cliente";
            case "AUDITOR" -> "Auditor";
            default -> null;
        };
    }

    private void addClaim(List<GrantedAuthority> authorities, List<String> values, String prefix) {
        if (values == null) return;
        values.stream()
                .flatMap(value -> List.of(value.split(" ")).stream())
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> new SimpleGrantedAuthority(prefix + value))
                .forEach(authorities::add);
    }
}