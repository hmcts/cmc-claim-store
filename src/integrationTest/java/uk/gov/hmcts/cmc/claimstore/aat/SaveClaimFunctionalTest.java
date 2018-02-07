package uk.gov.hmcts.cmc.claimstore.aat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.claimstore.RestTestClient;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    value = "/environment.properties",
    properties = {
        "document_management.api_gateway.url=false",
        "core_case_data.api.url=false"
    }
)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {BaseIntegrationTest.CleanDatabaseListener.class}, mergeMode = MERGE_WITH_DEFAULTS)
public class SaveClaimFunctionalTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RestTestClient restTestClient;

    @Test
    public void shouldReturnNewlyCreatedClaim() {
        System.out.println(">>> SaveClaimFunctionalTest : AppContext " + applicationContext);
        System.out.println(">>> SaveClaimFunctionalTest : AppContext hash " + applicationContext.hashCode());
//        System.out.println(">>> SaveClaimFunctionalTest : field UserService hash " + userService.hashCode());
        System.out.println(">>> SaveClaimFunctionalTest : AppContext UserService hash " + applicationContext
            .getBean(UserService.class).hashCode());
//        System.out.println(">>> ClaimService : CaseRepository hash " + caseRepository.hashCode());

        ClaimData claimData = SampleClaimData.submittedByClaimant();

        //        System.out.println(">>> SaveClaimFunctionalTest : calling with MockMvc");
        //        MvcResult result = makeRequest(claimData)
        //            .andExpect(status().isOk())
        //            .andReturn();
        //
        //        assertThat(deserializeObjectFrom(result, Claim.class))
        //            .extracting(Claim::getClaimData)
        //            .contains(claimData);

        System.out.println(">>> SaveClaimFunctionalTest : calling with RestAssured");
        Claim createdCase = restTestClient.post(claimData)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(createdCase.getClaimData()).isEqualTo(claimData);
    }

//    @Test
//    public void shouldFailWhenDuplicateExternalId() throws Exception {
//        UUID externalId = UUID.randomUUID();
//
//        ClaimData claimData = SampleClaimData.builder().withExternalId(externalId).build();
//        claimStore.saveClaim(claimData);
//
//        makeRequest(claimData)
//            .andExpect(status().isConflict());
//    }

}
