package uk.gov.hmcts.cmc.claimstore.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class JwtService {

    public boolean isCitizen(String authorisation) {
        final DecodedJWT decodedToken = JWT.decode(authorisation);
        final String[] data = decodedToken.getClaims().get("data").asString().split(",");
        return Arrays.stream(data).anyMatch(s -> s.equals("citizen"));
    }
}
