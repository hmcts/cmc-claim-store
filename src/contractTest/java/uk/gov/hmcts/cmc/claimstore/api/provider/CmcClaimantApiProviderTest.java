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
import uk.gov.hmcts.cmc.claimstore.controllers.ClaimController;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserInfo;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.cmc.claimstore.api.provider.ProviderTestUtils.getClaimResponse;

@ExtendWith(SpringExtension.class)
@Provider("cmc_claimant")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:80}",
    consumerVersionSelectors = {@VersionSelector(tag = "master")}
)
@ContextConfiguration(classes = {GetClaimsContractConfig.class})
@IgnoreNoPactsToVerify
public class CmcClaimantApiProviderTest {

    @Autowired
    private IdamApi idamApi;

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

    @State(value = "Get claimant cases")
    public Map toClaimantCases() {
        given(idamApi.retrieveUserInfo(eq("Bearer some-access-token")))
            .willReturn(UserInfo.builder().sub("user@email.com")
                .uid("100").givenName("firstName").familyName("surname").roles(List.of("caseworker-cmc")).build());

        given(ccdCaseApi.getBySubmitterId(eq("100"),
            eq("Bearer some-access-token"), eq(null)
        )).willReturn(List.of(getClaimResponse()));

        Map<String, Integer> map = new HashMap<>();
        map.put("submitterId", 100);

        return map;
    }
}
