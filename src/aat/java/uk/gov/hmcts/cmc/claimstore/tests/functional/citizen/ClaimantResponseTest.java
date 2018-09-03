package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimantResponseTest extends BaseTest {

    private User claimant;
    private Claim claim;

    @Before
    public void before() {
        claimant = idamTestService.createCitizen();

        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());
        claim = createClaimWithResponse(createdCase, defendant);
    }

    @Test
    public void shouldSaveClaimantResponseAcceptation() {
        commonOperations.submitClaimantResponse(
            SampleClaimantResponse.validDefaultAcceptation(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertThat(claimWithClaimantResponse.getClaimantRespondedAt().isPresent()).isTrue();
        ResponseAcceptation claimantResponse = (ResponseAcceptation) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(AssertionError::new);

        assertThat(claimantResponse.getAmountPaid()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void shouldSaveClaimantResponseRejection() {
        commonOperations.submitClaimantResponse(
            SampleClaimantResponse.validDefaultRejection(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertThat(claimWithClaimantResponse.getClaimantRespondedAt().isPresent()).isTrue();

        ResponseRejection claimantResponse = (ResponseRejection) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(AssertionError::new);

        assertThat(claimantResponse.getReason())
            .isEqualTo("He paid 10 but he actually owes 10,000. No I do not accept this.");
        assertThat(claimantResponse.isFreeMediation()).isFalse();
        assertThat(claimantResponse.getAmountPaid()).isEqualTo(BigDecimal.TEN);
    }

    private Claim createClaimWithResponse(Claim createdCase, User defendant) {
        commonOperations.linkDefendant(
            defendant.getAuthorisation()
        );

        Response response = SampleResponse.PartAdmission.builder()
            .build();

        return commonOperations.submitResponse(response, createdCase.getExternalId(), defendant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);
    }
}
