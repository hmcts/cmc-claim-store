package uk.gov.hmcts.cmc.claimstore.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@SuppressWarnings("squid:S1118")
@Component
public class AuthUtil {

    private final AuthTokenValidator authTokenValidator;
    private final List<String> allowedToPaymentUpdate;
    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String CODE = "code";

    private final UserService idamClient;

    @Autowired
    public AuthUtil(
        UserService idamClient,
                    AuthTokenValidator authTokenValidator,
                    @Value("${idam.s2s-auth.services-allowed-to-payment-update}") List<String> allowedToPaymentUpdate
    ) {
        this.idamClient = idamClient;
        this.authTokenValidator = authTokenValidator;
        this.allowedToPaymentUpdate = allowedToPaymentUpdate;
    }

    public String getBearToken(String token) {
        if (StringUtils.isBlank(token)) {
            return token;
        }

        return token.startsWith(BEARER) ? token : BEARER.concat(token);
    }

    public Boolean assertIsServiceAllowedToPaymentUpdate(String token) {
        String serviceName = this.authenticate(token);

        if (!allowedToPaymentUpdate.contains(serviceName)) {
            return false;
        }
        return true;
    }

    private String authenticate(String authHeader) {
        if (isBlank(authHeader)) {
            return null;
        }

        return authTokenValidator.getServiceName(authHeader);
    }

}
