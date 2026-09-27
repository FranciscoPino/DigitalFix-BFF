package cl.duoc.digitalfix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("securityProperties")
public class SecurityProperties {
    private final boolean enabled;

    public SecurityProperties(@Value("${app.security.enabled:false}") boolean enabled) {
        this.enabled = enabled;
    }

    public boolean disabled() {
        return !enabled;
    }
}