package cl.duoc.digitalfix.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtAudienceValidatorTest {
    private static final String API_CLIENT_ID = "613509df-3eb8-4469-9fc7-45b6cfd99801";

    @Test
    void acceptsClientIdGuidWhenConfiguredAudienceUsesApiUri() {
        Jwt token = jwtWithAudience(API_CLIENT_ID);
        OAuth2TokenValidatorResult result = new JwtAudienceValidator("api://" + API_CLIENT_ID).validate(token);

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void acceptsApiUriWhenConfiguredAudienceUsesClientIdGuid() {
        Jwt token = jwtWithAudience("api://" + API_CLIENT_ID);
        OAuth2TokenValidatorResult result = new JwtAudienceValidator(API_CLIENT_ID).validate(token);

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void rejectsAnUnrelatedAudience() {
        Jwt token = jwtWithAudience("another-api-client-id");
        OAuth2TokenValidatorResult result = new JwtAudienceValidator("api://" + API_CLIENT_ID).validate(token);

        assertThat(result.hasErrors()).isTrue();
    }

    private Jwt jwtWithAudience(String audience) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .audience(List.of(audience))
                .build();
    }
}
