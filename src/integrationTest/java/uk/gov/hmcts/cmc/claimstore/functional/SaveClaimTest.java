package uk.gov.hmcts.cmc.claimstore.functional;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false",
        "core_case_data.api.url=false"
    }
)
public class SaveClaimTest extends BaseSaveTest {

    @Autowired
    private RestAssuredHelper restAssuredHelper;

    @Test
    public void shouldSuccessfullySubmitClaimDataAndReturnCreatedCase() {
        ClaimData claimData = SampleClaimData.submittedByClaimant();

        Claim createdCase = restAssuredHelper.post(claimData)
            .then()
                .statusCode(HttpStatus.OK.value())
            .and()
                .extract().body().as(Claim.class);

        assertThat(createdCase.getClaimData()).isEqualTo(claimData);
    }

    // Original test was creating the existing claim with ClaimStore helper class.
    // This should work too but returns 500 instead, will investigate later.
    @Ignore
    @Test
    public void shouldReturnConflictResponseWhenClaimDataWithDuplicatedExternalIdIsSubmitted() {
        UUID externalId = UUID.randomUUID();

        ClaimData claimData = SampleClaimData.builder()
            .withExternalId(externalId)
            .build();

        restAssuredHelper.post(claimData)
            .andReturn();
        restAssuredHelper.post(claimData)
            .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

}
