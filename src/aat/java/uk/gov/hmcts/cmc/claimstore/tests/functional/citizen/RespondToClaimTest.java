package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RespondToClaimTest extends BaseTest {

    private User claimant;

    @Before
    public void before() {
        claimant = idamTestService.createCitizen();
    }

    @Test
    public void shouldBeAbleToSuccessfullySubmitDisputeDefence() {
        String defendantCollectionId = UUID.randomUUID().toString();

        Response fullDefenceDisputeResponse = SampleResponse.FullDefence
            .builder()
            .withDefenceType(DefenceType.DISPUTE)
            .withMediation(null)
            .withDefendantDetails(SampleParty.builder().withCollectionId(defendantCollectionId).individual())
            .build();

        shouldBeAbleToSuccessfullySubmit(fullDefenceDisputeResponse, defendantCollectionId);
    }

    @Test
    public void shouldBeAbleToSuccessfullySubmitFreeMediationRequestOnDefence() {
        String defendantCollectionId = UUID.randomUUID().toString();

        Response fullDefenceDisputeResponse = SampleResponse.FullDefence
            .builder()
            .withDefenceType(DefenceType.DISPUTE)
            .withMediation(YesNoOption.YES)
            .withDefendantDetails(SampleParty.builder().withCollectionId(defendantCollectionId).individual())
            .build();

        shouldBeAbleToSuccessfullySubmit(fullDefenceDisputeResponse, defendantCollectionId);
    }

    @Test
    public void shouldBeAbleToSuccessfullySubmitAlreadyPaidDefence() {
        String defendantCollectionId = UUID.randomUUID().toString();

        Response fullDefenceAlreadyPaidResponse = SampleResponse.FullDefence.builder()
            .withDefenceType(DefenceType.ALREADY_PAID)
            .withMediation(null)
            .withDefendantDetails(SampleParty.builder().withCollectionId(defendantCollectionId).individual())
            .build();

        shouldBeAbleToSuccessfullySubmit(fullDefenceAlreadyPaidResponse, defendantCollectionId);
    }

    @Test
    public void shouldBeAbleToSuccessfullySubmitFullAdmission() {
        String defendantCollectionId = UUID.randomUUID().toString();

        Response fullAdmissionResponse = SampleResponse.FullAdmission.builder()
            .withDefendantDetails(SampleParty.builder()
                .withCollectionId(defendantCollectionId)
                .individual())
            .build();
        shouldBeAbleToSuccessfullySubmit(fullAdmissionResponse, defendantCollectionId);
    }

    @Test
    public void shouldBeAbleToSuccessfullySubmitPartAdmissionWithAlreadyPaidAmount() {
        String defendantCollectionId = UUID.randomUUID().toString();

        Individual individual = SampleParty.builder().withCollectionId(defendantCollectionId).individual();
        Response partAdmissionResponse = SampleResponse.PartAdmission.builder()
            .buildWithPaymentIntentionAndParty(SamplePaymentIntention.bySetDate(), individual);

        shouldBeAbleToSuccessfullySubmit(partAdmissionResponse, defendantCollectionId);
    }

    @Test
    public void shouldBeAbleToSuccessfullySubmitPartAdmissionWithPaymentInFuture() {
        String defendantCollectionId = UUID.randomUUID().toString();

        Individual individual = SampleParty.builder().withCollectionId(defendantCollectionId).individual();
        Response partAdmissionResponse = SampleResponse.PartAdmission.builder()
            .buildWithPaymentIntentionAndParty(SamplePaymentIntention.bySetDate(), individual);

        shouldBeAbleToSuccessfullySubmit(partAdmissionResponse, defendantCollectionId);
    }

    private void shouldBeAbleToSuccessfullySubmit(Response response, String defendantCollectionId) {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations
            .submitClaimWithDefendantCollectionId(claimant.getAuthorisation(), claimantId, defendantCollectionId);

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());

        commonOperations.linkDefendant(defendant.getAuthorisation());

        Claim updatedCase = commonOperations.submitResponse(response, createdCase.getExternalId(), defendant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(updatedCase.getResponse().isPresent()).isTrue();
        assertThat(updatedCase.getResponse().get()).isEqualTo(response);
        assertThat(updatedCase.getRespondedAt()).isNotNull();
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenInvalidResponseIsSubmitted() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());
        commonOperations.linkDefendant(
            defendant.getAuthorisation()
        );

        Response invalidResponse = SampleResponse.FullDefence.builder()
            .withDefenceType(null)
            .build();

        commonOperations.submitResponse(invalidResponse, createdCase.getExternalId(), defendant)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }
}
