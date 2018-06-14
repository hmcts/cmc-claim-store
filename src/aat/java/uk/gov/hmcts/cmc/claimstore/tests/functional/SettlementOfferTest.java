package uk.gov.hmcts.cmc.claimstore.tests.functional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class SettlementOfferTest extends BaseTest {

    private User claimant;

    @Before
    public void before() {
        claimant = idamTestService.createCitizen();
    }

    @Test
    public void shouldBeAbleToSuccessfullySubmitOffer() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());
        Claim updatedCase = createClaimWithResponse(createdCase, defendant);

        Offer offer = SampleOffer.validDefaults();

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
    public void shouldFailForMultipleOfferFromOneUser() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());
        Claim updatedCase = createClaimWithResponse(createdCase, defendant);

        Offer offer = SampleOffer.validDefaults();

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
    public void shouldBeAbleToSuccessfullyAcceptOffer() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );
        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());
        Claim updatedCase = createClaimWithResponse(createdCase, defendant);

        Offer offer = SampleOffer.validDefaults();

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
    public void shouldFailAcceptOfferWithoutExistingOfferFromUser() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());
        Claim updatedCase = createClaimWithResponse(createdCase, defendant);

        commonOperations
            .acceptOffer(updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void shouldBeAbleToSuccessfullyRejectOffer() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());
        Claim updatedCase = createClaimWithResponse(createdCase, defendant);

        Offer offer = SampleOffer.validDefaults();

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
    public void shouldFailRejectOfferWithoutExistingOfferFromUser() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());
        Claim updatedCase = createClaimWithResponse(createdCase, defendant);

        commonOperations
            .rejectOffer(updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void shouldBeAbleToSuccessfullyCountersignOffer() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());

        Claim caseWithCounterSign = countersignAnOffer(createdCase, defendant);

        assertThat(caseWithCounterSign.getSettlement().isPresent()).isTrue();
        assertThat(caseWithCounterSign.getSettlement().get().getPartyStatements().size()).isEqualTo(3);

        assertThat(caseWithCounterSign.getSettlementReachedAt())
            .isCloseTo(LocalDateTimeFactory.nowInLocalZone(), within(2, ChronoUnit.MINUTES));
    }

    private Claim countersignAnOffer(Claim createdCase, User defendant) {

        Claim updatedCase = createClaimWithResponse(createdCase, defendant);

        Offer offer = SampleOffer.validDefaults();

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
    public void shouldFailRejectOfferWhenAlreadySettled() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());

        Claim updatedCase = countersignAnOffer(createdCase, defendant);

        commonOperations
            .rejectOffer(updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void shouldFailAcceptOfferWhenAlreadySettled() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());
        commonOperations.linkDefendant(
            defendant.getAuthorisation()
        );

        Claim updatedCase = countersignAnOffer(createdCase, defendant);

        commonOperations
            .acceptOffer(updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    private Claim createClaimWithResponse(Claim createdCase, User defendant) {

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
}
