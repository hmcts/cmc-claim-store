package uk.gov.hmcts.cmc.claimstore.aat;

import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
//import uk.gov.hmcts.cmc.claimstore.RestTestClient;
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

//    @Autowired
//    private RestTestClient restTestClient;

//    @Test
//    public void shouldSuccessfullySubmitClaimDataAndReturnCreatedCase() {
//        ClaimData claimData = SampleClaimData.submittedByClaimant();
//
//        Claim createdCase = restTestClient.post(claimData)
//            .then()
//            .statusCode(HttpStatus.OK.value())
//            .and()
//            .extract().body().as(Claim.class);
//
//        assertThat(createdCase.getClaimData()).isEqualTo(claimData);
//    }
//
//    @Test
//    public void shouldReturnConflictResponseWhenClaimDataWithDuplicatedExternalIdIsSubmitted() {
//        UUID externalId = UUID.randomUUID();
//
//        ClaimData claimData = SampleClaimData.submittedByClaimantBuilder()
//            .withExternalId(externalId)
//            .build();
//
//        restTestClient.post(claimData)
//            .andReturn();
//        restTestClient.post(claimData)
//            .then()
//            .statusCode(HttpStatus.CONFLICT.value());
//    }

    @Test
    public void shouldReturnNewlyCreatedClaim() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimant();

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);
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
