package uk.gov.hmcts.cmc.domain.models.offers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.exceptions.IllegalSettlementStatementException;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;
import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class SettlementTest {

    private static final String COUNTER_OFFER = "Get me a new roof instead";
    private static final Offer offer = SampleOffer.builder().build();

    @Mock
    private PartyStatement partyStatement;

    private Settlement settlement;

    @BeforeEach
    public void beforeEachTest() {
        settlement = new Settlement();
    }

    @Test
    public void partyStatementsShouldBeAnEmptyListForNewInstance() {
        assertThat(settlement.getPartyStatements()).isEmpty();
    }

    @Test
    public void returnedPartyStatementsShouldBeAnUnmodifiableList() {
        List<PartyStatement> statements = settlement.getPartyStatements();

        assertThrows(UnsupportedOperationException.class,  () -> {
            statements.add(partyStatement);
        });
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

    @Test
    public void partyIsNotAllowedToAcceptOfferWhenOfferWasNotMade() {
        assertThrows(IllegalSettlementStatementException.class,  () -> {
            settlement.accept(MadeBy.CLAIMANT, null);
        });
    }

    @Test
    public void partyIsNotAllowedToAcceptOfferWhenOfferWasAlreadyAccepted() {

        assertThrows(IllegalSettlementStatementException.class, () -> {
            settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
            settlement.accept(MadeBy.CLAIMANT, null);
            settlement.accept(MadeBy.CLAIMANT, null);
        });
    }

    @Test
    public void partyIsNotAllowedToAcceptOfferWhenTheOnlyOfferWasMadeByThemselves() {
        assertThrows(IllegalSettlementStatementException.class, () -> {
            settlement.makeOffer(offer, MadeBy.CLAIMANT, null);
            settlement.accept(MadeBy.CLAIMANT, null);
        });
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

    @Test
    public void partyIsNotAllowedToRejectOfferWhenOfferWasNotMade() {
        assertThrows(IllegalSettlementStatementException.class, () -> {
            settlement.reject(MadeBy.CLAIMANT, null);
        });
    }

    @Test
    public void partyIsNotAllowedToRejectOfferWhenOfferWasAlreadyRejected() {

        assertThrows(IllegalSettlementStatementException.class, () -> {
            settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
            settlement.reject(MadeBy.CLAIMANT, null);
            settlement.reject(MadeBy.CLAIMANT, null);
        });
    }

    @Test
    public void partyIsNotAllowedToRejectOfferWhenTheOnlyOfferWasMadeByThemselves() {

        assertThrows(IllegalSettlementStatementException.class, () -> {
            settlement.makeOffer(offer, MadeBy.CLAIMANT, null);
            settlement.reject(MadeBy.CLAIMANT, null);
        });
    }

    @Test
    public void partyCanCountersignAnAcceptedOffer() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        settlement.countersign(MadeBy.DEFENDANT, null);

        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.COUNTERSIGNATURE);
    }

    @Test
    public void partyIsNotAllowedToCountersignWhenOfferAlreadyCountersigned() {

        assertThrows(IllegalSettlementStatementException.class, () -> {
            settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
            settlement.accept(MadeBy.CLAIMANT, null);
            settlement.countersign(MadeBy.DEFENDANT, null);
            settlement.countersign(MadeBy.DEFENDANT, null);
        });
    }

    @Test
    public void partyIsNotAllowedToCountersignWhenOfferWasAcceptedByThemselves() {

        assertThrows(IllegalSettlementStatementException.class, () -> {
            settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
            settlement.accept(MadeBy.CLAIMANT, null);
            settlement.countersign(MadeBy.CLAIMANT, null);
        });
    }

    @Test
    public void partyIsNotAllowedToCountersignWhenOfferWasNotAccepted() {

        assertThrows(IllegalSettlementStatementException.class, () -> {
            settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
            settlement.countersign(MadeBy.DEFENDANT, null);
        });
    }

    @Test
    public void partyIsNotAllowedToCountersignWhenOfferWasRejected() {

        assertThrows(IllegalSettlementStatementException.class, () -> {
            settlement.makeOffer(offer, MadeBy.DEFENDANT, null);
            settlement.reject(MadeBy.CLAIMANT, null);
            settlement.countersign(MadeBy.DEFENDANT, null);
        });
    }

    @Test
    public void getLastStatementShouldThrowIllegalStateWhenNoStatementsHaveBeenMade() {

        assertThrows(IllegalSettlementStatementException.class, () -> {
            settlement.getLastStatement();
        });
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

    @Test
    public void getLastOfferStatementShouldThrowWhenNoStatements() {

        assertThrows(IllegalSettlementStatementException.class, () -> {
            settlement.getLastStatementOfType(StatementType.OFFER);
        });
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
