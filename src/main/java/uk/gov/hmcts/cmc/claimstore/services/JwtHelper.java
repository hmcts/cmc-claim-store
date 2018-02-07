package uk.gov.hmcts.cmc.claimstore.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class JwtHelper {

    public boolean isSolicitor(String authorisation) {
        final DecodedJWT decodedToken = JWT.decode(authorisation.replace("Bearer ", ""));
        final String[] data = decodedToken.getClaims().get("data").asString().split(",");
        return Arrays.stream(data).anyMatch("solicitor"::equals);
    }
}
