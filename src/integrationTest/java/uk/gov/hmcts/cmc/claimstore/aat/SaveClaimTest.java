package uk.gov.hmcts.cmc.claimstore.aat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.claimstore.RestTestClient;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false",
        "core_case_data.api.url=false"
    }
)
public class SaveClaimTest extends BaseSaveTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RestTestClient restTestClient;

    @Test
    public void shouldReturnNewlyCreatedClaim() {
        System.out.println(">>> SaveClaimTest : AppContext " + applicationContext);
        System.out.println(">>> SaveClaimTest : AppContext hash " + applicationContext.hashCode());
        System.out.println(">>> SaveClaimTest : field UserService hash " + userService.hashCode());
        System.out.println(">>> SaveClaimTest : AppContext UserService hash " + applicationContext
            .getBean(UserService.class));
        System.out.println(">>> ClaimService : CaseRepository hash " + caseRepository.hashCode());

        ClaimData claimData = SampleClaimData.submittedByClaimant();

        //        System.out.println(">>> SaveClaimTest : calling with MockMvc");
        //        MvcResult result = makeRequest(claimData)
        //            .andExpect(status().isOk())
        //            .andReturn();
        //
        //        assertThat(deserializeObjectFrom(result, Claim.class))
        //            .extracting(Claim::getClaimData)
        //            .contains(claimData);

        System.out.println(">>> SaveClaimTest : calling with RestAssured");
        Claim createdCase = restTestClient.post(claimData)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(createdCase.getClaimData()).isEqualTo(claimData);
    }

    @Test
    public void shouldFailWhenDuplicateExternalId() throws Exception {
        UUID externalId = UUID.randomUUID();

        ClaimData claimData = SampleClaimData.builder().withExternalId(externalId).build();
        claimStore.saveClaim(claimData);

        makeRequest(claimData)
            .andExpect(status().isConflict());
    }

}
