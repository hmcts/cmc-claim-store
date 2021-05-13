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
        Claim createdCase = submitClaimSynchronized();

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
        Claim createdCase = submitClaimSynchronized();

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
        Claim createdCase = submitClaimSynchronized();
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
        Claim createdCase = submitClaimSynchronized();

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
        Claim createdCase = submitClaimSynchronized();

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
        Claim createdCase = submitClaimSynchronized();

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
        Claim createdCase = submitClaimSynchronized();

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
        Claim createdCase = submitClaimSynchronized();

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
        Claim createdCase = submitClaimSynchronized();

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        commonOperations.linkDefendant(
            defendant.getAuthorisation(), createdCase.getLetterHolderId()
        );

        Claim updatedCase = countersignAnOffer(createdCase, defendant);

        commonOperations
            .acceptOffer(updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    private Claim createClaimWithDisputeResponse(Claim createdCase, User defendant) {

        commonOperations.linkDefendant(
            defendant.getAuthorisation(), createdCase.getLetterHolderId()
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
        Claim createdCase = submitClaimSynchronized();

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        commonOperations.linkDefendant(
            defendant.getAuthorisation(), createdCase.getLetterHolderId()
        );

        Response response = SampleResponse.FullAdmission.builder().build();

        return commonOperations.submitResponse(response, createdCase.getExternalId(), defendant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);
    }

    private Claim submitClaimSynchronized() {
        Claim synchronizedClaim = null;
        synchronized (SettlementOfferTest.class) {
            String claimantId = claimant.getUserDetails().getId();
            synchronizedClaim = commonOperations.submitClaim(
                claimant.getAuthorisation(),
                claimantId
            );
        }
        return synchronizedClaim;
    }
}
