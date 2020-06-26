package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccj.InterlocutoryJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccj.ReDeterminationEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.RejectOrganisationPaymentPlanEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferAcceptedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferMadeEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferRejectedEvent;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantPaperResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.events.revieworder.ReviewOrderEvent;
import uk.gov.hmcts.cmc.claimstore.events.settlement.CountersignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.events.settlement.RejectSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.events.settlement.SignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.time.LocalDate;
import java.util.List;

@Component
public class EventProducer {
    private final ApplicationEventPublisher publisher;

    @Autowired
    public EventProducer(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void createClaimIssuedEvent(Claim claim, String pin, String submitterName, String authorisation) {
        if (claim.getClaimData().isClaimantRepresented()) {
            publisher.publishEvent(new RepresentedClaimIssuedEvent(claim, submitterName, authorisation));
        } else {
            publisher.publishEvent(new CitizenClaimIssuedEvent(claim, pin, submitterName, authorisation));
        }
    }

    public void createClaimCreatedEvent(Claim claim, String submitterName, String authorisation) {

        if (claim.getClaimData().isClaimantRepresented()) {
            publisher.publishEvent(new RepresentedClaimCreatedEvent(claim, submitterName, authorisation));
        } else {
            publisher.publishEvent(new CitizenClaimCreatedEvent(claim, submitterName, authorisation));
        }
    }

    public void createRepresentedClaimCreatedEvent(Claim claim, String submitterName, String authorisation) {
        publisher.publishEvent(new RepresentedClaimCreatedEvent(claim, submitterName, authorisation));
    }

    public void createDefendantResponseEvent(Claim claim, String authorisation) {
        publisher.publishEvent(new DefendantResponseEvent(claim, authorisation));
    }

    public void createDefendantPaperResponseEvent(Claim claim, String authorisation) {
        publisher.publishEvent(new DefendantPaperResponseEvent(claim, authorisation));
    }

    public void createMoreTimeForResponseRequestedEvent(
        Claim claim,
        LocalDate newResponseDeadline,
        String defendantEmail
    ) {
        publisher.publishEvent(new MoreTimeRequestedEvent(claim, newResponseDeadline, defendantEmail));
    }

    public void createCountyCourtJudgmentEvent(Claim claim, String authorisation) {
        publisher.publishEvent(new CountyCourtJudgmentEvent(claim, authorisation));
    }

    public void createOfferMadeEvent(Claim claim) {
        publisher.publishEvent(new OfferMadeEvent(claim));
    }

    public void createOfferAcceptedEvent(Claim claim, MadeBy party) {
        publisher.publishEvent(new OfferAcceptedEvent(claim, party));
    }

    public void createOfferRejectedEvent(Claim claim, MadeBy party) {
        publisher.publishEvent(new OfferRejectedEvent(claim, party));
    }

    public void createRejectSettlementAgreementEvent(Claim claim) {
        publisher.publishEvent(new RejectSettlementAgreementEvent(claim));
    }

    public void createSettlementAgreementCountersignedEvent(Claim claim, String authorisation) {
        publisher.publishEvent(new CountersignSettlementAgreementEvent(claim, authorisation));
    }

    public void createAgreementCountersignedEvent(Claim claim, MadeBy party, String authorisation) {
        publisher.publishEvent(new AgreementCountersignedEvent(claim, party, authorisation));
    }

    public void createSignSettlementAgreementEvent(Claim claim) {
        publisher.publishEvent(new SignSettlementAgreementEvent(claim));
    }

    public void createClaimantResponseEvent(Claim claim, String authorisation) {
        publisher.publishEvent(new ClaimantResponseEvent(claim, authorisation));
    }

    public void createPaidInFullEvent(Claim claim) {
        publisher.publishEvent(new PaidInFullEvent(claim));
    }

    public void createRedeterminationEvent(Claim claim, String authorisation, String submitterName, MadeBy partyType) {
        publisher.publishEvent(new ReDeterminationEvent(claim, authorisation, submitterName, partyType));
    }

    public void createInterlocutoryJudgmentEvent(Claim claim) {
        publisher.publishEvent(new InterlocutoryJudgmentEvent(claim));
    }

    public void createRejectOrganisationPaymentPlanEvent(Claim claim) {
        publisher.publishEvent(new RejectOrganisationPaymentPlanEvent(claim));
    }

    public void createReviewOrderEvent(String authorisation, Claim claim) {
        publisher.publishEvent(new ReviewOrderEvent(authorisation, claim));
    }

    public void createBulkPrintTransferEvent(
        Claim claim,
        Document coverLetter,
        List<BulkPrintTransferEvent.PrintableDocument> caseDocuments,
        String authorisation
    ) {
        publisher.publishEvent(new BulkPrintTransferEvent(claim, coverLetter, caseDocuments, authorisation));
    }
}
