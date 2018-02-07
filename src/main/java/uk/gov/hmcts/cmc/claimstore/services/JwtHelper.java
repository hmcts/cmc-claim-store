package uk.gov.hmcts.cmc.claimstore.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class JwtHelper {

    private static final String BEARER_PREFIX = "Bearer";

    public boolean isSolicitor(String authorisation) {
        DecodedJWT decodedToken = JWT.decode(sanitizeToken(authorisation));
        String[] data = decodedToken.getClaims().get("data").asString().split(",");
        return Arrays.stream(data).anyMatch("solicitor"::equals);
    }

    public String getUserId(String authorisation) {
        DecodedJWT decodedToken = JWT.decode(sanitizeToken(authorisation));
        return decodedToken.getClaims().get("id").asString();
    }

    private String sanitizeToken(String authorisationToken) {
        if (authorisationToken.startsWith(BEARER_PREFIX)) {
            return authorisationToken.replace(BEARER_PREFIX, "");
        }
        return authorisationToken;
    }

}
