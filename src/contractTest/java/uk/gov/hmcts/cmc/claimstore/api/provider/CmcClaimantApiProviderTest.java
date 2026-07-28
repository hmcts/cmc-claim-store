package uk.gov.hmcts.cmc.claimstore.api.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
@PactBroker(
    url = "${PACT_BROKER_FULL_URL:http://localhost:80}",
    providerBranch = "${pact.provider.branch:master}"
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

    @PactBrokerConsumerVersionSelectors
    public static SelectorBuilder consumerVersionSelectors() {
        return new SelectorBuilder()
            .matchingBranch()
            .mainBranch()
            .deployedOrReleased();
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", "true");

        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new Jdk8Module())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2HttpMessageConverter converter =
            new MappingJackson2HttpMessageConverter(objectMapper);

        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(claimController)
            .setMessageConverters(converter)
            .build();

        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setMockMvc(mockMvc);
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
