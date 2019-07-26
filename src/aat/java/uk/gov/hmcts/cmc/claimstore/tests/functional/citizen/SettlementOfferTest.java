package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

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

import static org.assertj.core.api.Assertions.assertThat;

public class SettlementOfferTest extends BaseTest {

    private User claimant;

    @Before
    public void before() {
        claimant = idamTestService.createCitizen();
    }

    @Test
    public void shouldBeAbleToSuccessfullySubmitOffer() {
        User defendant = idamTestService.createDefendant("abc");

        Response response = SampleResponse.FullDefence.builder()
            .withDefenceType(DefenceType.DISPUTE)
            .build();

        Claim createdCase = commonOperations.submitClaimViaTestingSupport(
            claimant.getAuthorisation(),
            defendant.getUserDetails().getEmail(),
            response
        );

        Offer offer = SampleOffer.builder().build();

        Claim caseWithOffer = commonOperations
            .submitOffer(offer, createdCase.getExternalId(), defendant.getAuthorisation(), MadeBy.DEFENDANT)
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
        User defendant = idamTestService.createDefendant("abc");
        Response response = SampleResponse.FullDefence.builder()
            .withDefenceType(DefenceType.DISPUTE)
            .build();

        Claim createdCase = commonOperations.submitClaimViaTestingSupport(
            claimant.getAuthorisation(),
            defendant.getUserDetails().getEmail(),
            response
        );

        Offer offer = SampleOffer.builder().build();

        commonOperations
            .submitOffer(offer, createdCase.getExternalId(), defendant.getAuthorisation(), MadeBy.DEFENDANT)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .and()
            .extract().body().as(Claim.class);

        commonOperations
            .submitOffer(offer, createdCase.getExternalId(), defendant.getAuthorisation(), MadeBy.DEFENDANT)
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldBeAbleToSuccessfullyAcceptOffer() {
        User defendant = idamTestService.createDefendant("abc");
        Response response = SampleResponse.FullDefence.builder()
            .withDefenceType(DefenceType.DISPUTE)
            .build();

        Claim createdCase = commonOperations.submitClaimViaTestingSupport(
            claimant.getAuthorisation(),
            defendant.getUserDetails().getEmail(),
            response
        );

        Offer offer = SampleOffer.builder().build();

        Claim caseWithOffer = commonOperations
            .submitOffer(offer, createdCase.getExternalId(), defendant.getAuthorisation(), MadeBy.DEFENDANT)
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
        User defendant = idamTestService.createDefendant("abc");
        Response response = SampleResponse.FullDefence.builder()
            .withDefenceType(DefenceType.DISPUTE)
            .build();

        Claim createdCase = commonOperations.submitClaimViaTestingSupport(
            claimant.getAuthorisation(),
            defendant.getUserDetails().getEmail(),
            response
        );

        commonOperations
            .acceptOffer(createdCase.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void shouldBeAbleToSuccessfullyRejectOffer() {
        User defendant = idamTestService.createDefendant("abc");
        Response response = SampleResponse.FullDefence.builder()
            .withDefenceType(DefenceType.DISPUTE)
            .build();

        Claim createdCase = commonOperations.submitClaimViaTestingSupport(
            claimant.getAuthorisation(),
            defendant.getUserDetails().getEmail(),
            response
        );

        Offer offer = SampleOffer.builder().build();

        Claim caseWithOffer = commonOperations
            .submitOffer(offer, createdCase.getExternalId(), defendant.getAuthorisation(), MadeBy.DEFENDANT)
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
        User defendant = idamTestService.createDefendant("abc");
        Response response = SampleResponse.FullDefence.builder()
            .withDefenceType(DefenceType.DISPUTE)
            .build();

        Claim createdCase = commonOperations.submitClaimViaTestingSupport(
            claimant.getAuthorisation(),
            defendant.getUserDetails().getEmail(),
            response
        );

        commonOperations
            .rejectOffer(createdCase.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void shouldBeAbleToSuccessfullyCountersignOffer() {
        User defendant = idamTestService.createDefendant("abc");
        Claim caseWithCounterSign = countersignAnOffer(defendant);

        assertThat(caseWithCounterSign.getSettlement().isPresent()).isTrue();
        assertThat(caseWithCounterSign.getSettlement().get().getPartyStatements().size()).isEqualTo(3);
        assertThat(caseWithCounterSign.getSettlementReachedAt()).isNotNull();
    }

    private Claim countersignAnOffer(User defendant) {
        Response response = SampleResponse.FullDefence.builder()
            .withDefenceType(DefenceType.DISPUTE)
            .build();

        Claim createdCase = commonOperations.submitClaimViaTestingSupport(
            claimant.getAuthorisation(),
            defendant.getUserDetails().getEmail(),
            response
        );

        Offer offer = SampleOffer.builder().build();

        Claim caseWithOffer = commonOperations
            .submitOffer(offer, createdCase.getExternalId(), defendant.getAuthorisation(), MadeBy.DEFENDANT)
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
        User defendant = idamTestService.createDefendant("abc");
        Claim updatedCase = countersignAnOffer(defendant);

        commonOperations
            .rejectOffer(updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void shouldFailAcceptOfferWhenAlreadySettled() {
        User defendant = idamTestService.createDefendant("abc");
        Claim updatedCase = countersignAnOffer(defendant);

        commonOperations
            .acceptOffer(updatedCase.getExternalId(), defendant.getAuthorisation(), MadeBy.CLAIMANT)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }
}
