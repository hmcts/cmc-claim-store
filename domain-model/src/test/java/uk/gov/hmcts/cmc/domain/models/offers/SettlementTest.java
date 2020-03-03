package uk.gov.hmcts.cmc.domain.models.offers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.exceptions.IllegalSettlementStatementException;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SettlementTest {

    private static final String COUNTER_OFFER = "Get me a new roof instead";
    private static final Offer offer = SampleOffer.builder().build();

    @Mock
    private PartyStatement partyStatement;

    private Settlement settlement;

    @Before
    public void beforeEachTest() {
        settlement = new Settlement();
    }

    @Test
    public void partyStatementsShouldBeAnEmptyListForNewInstance() {
        assertThat(settlement.getPartyStatements()).isEmpty();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void returnedPartyStatementsShouldBeAnUnmodifiableList() {
        List<PartyStatement> statements = settlement.getPartyStatements();
        statements.add(partyStatement);
    }

    @Test
    public void getLastStatementShouldReturnLastStatement() {
        Offer counterOffer = SampleOffer.builder()
            .content(COUNTER_OFFER)
            .build();

        settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
        settlement.makeOffer(counterOffer, MadeBy.CLAIMANT, null);

        assertThat(settlement.getLastStatement().getOffer().orElse(null)).isEqualTo(counterOffer);
    }

    @Test
    public void claimantCanAcceptAnOffer() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.ACCEPTATION);
    }

    @Test
    public void defendantCanAcceptAnOffer() {
        settlement.makeOffer(offer, MadeBy.CLAIMANT, null);
        settlement.accept(MadeBy.DEFENDANT, null);

        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.ACCEPTATION);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToAcceptOfferWhenOfferWasNotMade() {
        settlement.accept(MadeBy.CLAIMANT, null);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToAcceptOfferWhenOfferWasAlreadyAccepted() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        settlement.accept(MadeBy.CLAIMANT, null);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToAcceptOfferWhenTheOnlyOfferWasMadeByThemselves() {
        settlement.makeOffer(offer, MadeBy.CLAIMANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);
    }

    @Test
    public void claimantCanRejectAnOffer() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
        settlement.reject(MadeBy.CLAIMANT, null);

        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.REJECTION);
    }

    @Test
    public void defendantCanRejectAnOffer() {
        settlement.makeOffer(offer, MadeBy.CLAIMANT, null);
        settlement.reject(MadeBy.DEFENDANT, null);

        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.REJECTION);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToRejectOfferWhenOfferWasNotMade() {
        settlement.reject(MadeBy.CLAIMANT, null);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToRejectOfferWhenOfferWasAlreadyRejected() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
        settlement.reject(MadeBy.CLAIMANT, null);

        settlement.reject(MadeBy.CLAIMANT, null);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToRejectOfferWhenTheOnlyOfferWasMadeByThemselves() {
        settlement.makeOffer(offer, MadeBy.CLAIMANT, null);
        settlement.reject(MadeBy.CLAIMANT, null);
    }

    @Test
    public void partyCanCountersignAnAcceptedOffer() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        settlement.countersign(MadeBy.DEFENDANT, null);

        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.COUNTERSIGNATURE);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToCountersignWhenOfferAlreadyCountersigned() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);
        settlement.countersign(MadeBy.DEFENDANT, null);

        settlement.countersign(MadeBy.DEFENDANT, null);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToCountersignWhenOfferWasAcceptedByThemselves() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        settlement.countersign(MadeBy.CLAIMANT, null);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToCountersignWhenOfferWasNotAccepted() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT, null);

        settlement.countersign(MadeBy.DEFENDANT, null);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToCountersignWhenOfferWasRejected() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
        settlement.reject(MadeBy.CLAIMANT, null);

        settlement.countersign(MadeBy.DEFENDANT, null);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void getLastStatementShouldThrowIllegalStateWhenNoStatementsHaveBeenMade() {
        settlement.getLastStatement();
    }

    @Test
    public void getLastOfferStatementShouldGiveLastStatementThatIsAnOffer() {
        settlement.makeOffer(offer, MadeBy.CLAIMANT, null);
        settlement.reject(MadeBy.DEFENDANT, null);

        Offer counterOffer = SampleOffer.builder()
            .content(COUNTER_OFFER)
            .build();

        settlement.makeOffer(counterOffer, MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        assertThat(settlement.getLastStatementOfType(StatementType.OFFER).getOffer())
            .isPresent().get()
            .extracting(Offer::getContent)
            .isEqualTo(COUNTER_OFFER);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void getLastOfferStatementShouldThrowWhenNoStatements() {
        settlement.getLastStatementOfType(StatementType.OFFER);
    }

    @Test
    public void isSettlementThroughAdmissionsShouldShouldReturnTrueForAdmissionsRoute() {
        Offer admissionOffer = Offer.builder()
            .content("Defendant full admission offer")
            .completionDate(now().plusDays(14))
            .paymentIntention(PaymentIntention.builder().build())
            .build();

        settlement.makeOffer(admissionOffer, MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        assertThat(settlement.isSettlementThroughAdmissions()).isTrue();
    }

    @Test
    public void isSettlementThroughAdmissionsShouldShouldReturnFalseForOffersRoute() {
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        assertThat(settlement.isSettlementThroughAdmissions()).isFalse();
    }

}
