package uk.gov.hmcts.cmc.claimstore.tests.functional;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SaveClaimTest extends BaseTest {

    @Test
    public void shouldSuccessfullySubmitClaimDataAndReturnCreatedCase() {
        ClaimData claimData = SampleClaimData.submittedByClaimant();

        Claim createdCase = saveClaim(claimData)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(createdCase.getClaimData()).isEqualTo(claimData);
    }

    @Test
    public void shouldReturnConflictResponseWhenClaimDataWithDuplicatedExternalIdIsSubmitted() {
        UUID externalId = UUID.randomUUID();

        ClaimData claimData = SampleClaimData.submittedByClaimantBuilder()
            .withExternalId(externalId)
            .build();

        saveClaim(claimData)
            .andReturn();
        saveClaim(claimData)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

}
