package uk.gov.hmcts.cmc.claimstore.api.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworker;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworkerProperties;
import uk.gov.hmcts.cmc.claimstore.controllers.ClaimController;
import uk.gov.hmcts.cmc.claimstore.models.idam.TokenExchangeResponse;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserInfo;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.cmc.claimstore.api.provider.ProviderTestUtils.getClaimResponse;

@ExtendWith(SpringExtension.class)
@Provider("cmc_defendantLinked")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:80}",
    consumerVersionSelectors = {@VersionSelector(tag = "master")}
)
@ContextConfiguration(classes = {GetClaimsContractConfig.class})
@IgnoreNoPactsToVerify
public class CmcDefendantLinkedApiProviderTest {

    @Autowired
    private IdamApi idamApi;

    @Autowired
    private IdamCaseworkerProperties idamCaseworkerProperties;

    @Autowired
    private CCDCaseApi ccdCaseApi;

    @Autowired
    private ClaimController claimController;

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        testTarget.setControllers(claimController);
        if (context != null) {
            context.setTarget(testTarget);
        }
    }

    @State(value = "Get claimant linked cases status")
    public Map toDefendantLinkedCaseStatus() {

        IdamCaseworker idamCaseworker = new IdamCaseworker();
        idamCaseworker.setPassword("somePassw");
        idamCaseworker.setUsername("user@email.com");

        given(idamCaseworkerProperties.getAnonymous()).willReturn(idamCaseworker);

        TokenExchangeResponse tokenExchangeResponse = new TokenExchangeResponse("some-access-token");

        given(idamApi.exchangeToken(any(), any(), any(), eq("password"),
            eq("user@email.com"), eq("somePassw"), eq("openid profile roles")))
            .willReturn(tokenExchangeResponse);

        given(idamApi.retrieveUserInfo(eq("Bearer some-access-token")))
            .willReturn(UserInfo.builder().sub("user@email.com")
                .uid("200").givenName("firstName").familyName("surname").roles(List.of("caseworker-cmc")).build());

        given(ccdCaseApi.getByReferenceNumber(eq("100"),
            eq("Bearer some-access-token")
        )).willReturn(Optional.of(getClaimResponse()));

        Map<String, Integer> map = new HashMap<>();
        map.put("caseReference", 100);

        return map;
    }
}
