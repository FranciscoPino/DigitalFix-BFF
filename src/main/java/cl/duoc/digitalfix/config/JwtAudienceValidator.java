package cl.duoc.digitalfix.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Locale;

public class JwtAudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final String expectedAudience;

    public JwtAudienceValidator(String expectedAudience) {
        this.expectedAudience = expectedAudience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String expected = normalize(expectedAudience);
        if (token.getAudience() != null && token.getAudience().stream().anyMatch(audience -> normalize(audience).equals(expected))) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                "invalid_token", "The required audience is missing", null));
    }

    private String normalize(String audience) {
        String value = audience == null ? "" : audience.trim();
        if (value.regionMatches(true, 0, "api://", 0, 6)) {
            value = value.substring(6);
        }
        return value.replaceAll("/$", "").toLowerCase(Locale.ROOT);
    }
}