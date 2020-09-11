package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.Retry;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.RetryFailedFunctionalTests;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import static org.assertj.core.api.Assertions.assertThat;

public class SettlementOfferTest extends BaseTest {

    private User claimant;

    @Before
    public void before() {
        claimant = bootstrap.getClaimant();
    }

    @Rule
    public RetryFailedFunctionalTests retryRule = new RetryFailedFunctionalTests(3);

    @Test
    @Retry
    public void shouldBeAbleToSuccessfullySubmitOffer() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        Claim updatedCase = createClaimWithDisputeResponse(createdCase, defendant);

        Offer offer = SampleOffer.builder().build();

        Claim caseWithOffer = commonOperations
            .submitOffer(offer, updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.DEFENDANT)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(caseWithOffer.getSettlement().isPresent()).isTrue();
        assertThat(caseWithOffer.getSettlement().get().getPartyStatements().size()).isEqualTo(1);
        assertThat(caseWithOffer.getSettlementReachedAt()).isNull();
    }

    @Test
    @Retry
    public void shouldFailForMultipleOfferFromOneUser() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        Claim updatedCase = createClaimWithDisputeResponse(createdCase, defendant);

        Offer offer = SampleOffer.builder().build();

        commonOperations
            .submitOffer(offer, updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.DEFENDANT)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .and()
            .extract().body().as(Claim.class);

        commonOperations
            .submitOffer(offer, updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.DEFENDANT)
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Retry
    public void shouldBeAbleToSuccessfullyAcceptOffer() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );
        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        Claim updatedCase = createClaimWithDisputeResponse(createdCase, defendant);

        Offer offer = SampleOffer.builder().build();

        Claim caseWithOffer = commonOperations
            .submitOffer(offer, updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.DEFENDANT)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .and()
            .extract().body().as(Claim.class);

        Claim caseWithAcceptance = commonOperations
            .acceptOffer(caseWithOffer.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(caseWithAcceptance.getSettlement().isPresent()).isTrue();
        assertThat(caseWithAcceptance.getSettlement().get().getPartyStatements().size()).isEqualTo(2);
        assertThat(caseWithAcceptance.getSettlementReachedAt()).isNull();
    }

    @Test
    @Retry
    public void shouldFailAcceptOfferWithoutExistingOfferFromUser() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        Claim updatedCase = createClaimWithDisputeResponse(createdCase, defendant);

        commonOperations
            .acceptOffer(updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @Retry
    public void shouldBeAbleToSuccessfullyRejectOffer() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        Claim updatedCase = createClaimWithDisputeResponse(createdCase, defendant);

        Offer offer = SampleOffer.builder().build();

        Claim caseWithOffer = commonOperations
            .submitOffer(offer, updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.DEFENDANT)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .and()
            .extract().body().as(Claim.class);

        Claim caseWithAcceptance = commonOperations
            .rejectOffer(caseWithOffer.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(caseWithAcceptance.getSettlement().isPresent()).isTrue();
        assertThat(caseWithAcceptance.getSettlement().get().getPartyStatements().size()).isEqualTo(2);
        assertThat(caseWithAcceptance.getSettlementReachedAt()).isNull();
    }

    @Test
    @Retry
    public void shouldFailRejectOfferWithoutExistingOfferFromUser() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        Claim updatedCase = createClaimWithDisputeResponse(createdCase, defendant);

        commonOperations
            .rejectOffer(updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @Retry
    public void shouldBeAbleToSuccessfullyCountersignOffer() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());

        Claim caseWithCounterSign = countersignAnOffer(createdCase, defendant);

        assertThat(caseWithCounterSign.getSettlement().isPresent()).isTrue();
        assertThat(caseWithCounterSign.getSettlement().get().getPartyStatements().size()).isEqualTo(3);
        assertThat(caseWithCounterSign.getSettlementReachedAt()).isNotNull();
    }

    private Claim countersignAnOffer(Claim createdCase, User defendant) {

        Claim updatedCase = createClaimWithDisputeResponse(createdCase, defendant);

        Offer offer = SampleOffer.builder().build();

        Claim caseWithOffer = commonOperations
            .submitOffer(offer, updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.DEFENDANT)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .and()
            .extract().body().as(Claim.class);

        Claim caseWithAcceptance = commonOperations
            .acceptOffer(caseWithOffer.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .and()
            .extract().body().as(Claim.class);

        return commonOperations
            .countersignOffer(caseWithAcceptance.getExternalId(), defendant.getAuthorisation(), MadeBy.DEFENDANT)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .and()
            .extract().body().as(Claim.class);
    }

    @Test
    @Retry
    public void shouldFailRejectOfferWhenAlreadySettled() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());

        Claim updatedCase = countersignAnOffer(createdCase, defendant);

        commonOperations
            .rejectOffer(updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @Retry
    public void shouldFailAcceptOfferWhenAlreadySettled() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        commonOperations.linkDefendant(
            defendant.getAuthorisation()
        );

        Claim updatedCase = countersignAnOffer(createdCase, defendant);

        commonOperations
            .acceptOffer(updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    private Claim createClaimWithDisputeResponse(Claim createdCase, User defendant) {

        commonOperations.linkDefendant(
            defendant.getAuthorisation()
        );

        Response response = SampleResponse.FullDefence.builder()
            .withDefenceType(DefenceType.DISPUTE)
            .build();

        return commonOperations.submitResponse(response, createdCase.getExternalId(), defendant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);
    }

    private Claim createClaimWithFullAdmissionResponse() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        commonOperations.linkDefendant(
            defendant.getAuthorisation()
        );

        Response response = SampleResponse.FullAdmission.builder().build();

        return commonOperations.submitResponse(response, createdCase.getExternalId(), defendant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);
    }
}
