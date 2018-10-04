package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDClaimantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDCountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDDefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDLinkDefendantEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDLinkSealedClaimDocumentEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDMoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDPrePaymentEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDSettlementEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferAcceptedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferMadeEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferRejectedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.SignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;

import java.net.URI;
import java.time.LocalDate;

@Component
public class EventProducer {
    private final ApplicationEventPublisher publisher;

    public EventProducer(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void createClaimIssuedEvent(Claim claim, String pin,
                                       String submitterName, String authorisation) {

        if (claim.getClaimData().isClaimantRepresented()) {
            publisher.publishEvent(new RepresentedClaimIssuedEvent(claim, submitterName, authorisation));
        } else {
            publisher.publishEvent(new CitizenClaimIssuedEvent(claim, pin, submitterName, authorisation));
        }
    }

    public void createDefendantResponseEvent(Claim claim) {
        publisher.publishEvent(new DefendantResponseEvent(claim));
    }

    public void createMoreTimeForResponseRequestedEvent(
        Claim claim, LocalDate newResponseDeadline, String defendantEmail) {
        publisher.publishEvent(new MoreTimeRequestedEvent(claim, newResponseDeadline, defendantEmail));
    }

    public void createCountyCourtJudgmentEvent(Claim claim, String authorisation, boolean issue) {
        publisher.publishEvent(new CountyCourtJudgmentEvent(claim, authorisation, issue));
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

    public void createAgreementCountersignedEvent(Claim claim, MadeBy party) {
        publisher.publishEvent(new AgreementCountersignedEvent(claim, party));
    }

    public void createSignSettlementAgreementEvent(Claim claim) {
        publisher.publishEvent(new SignSettlementAgreementEvent(claim));
    }

    public void createClaimantResponseEvent(Claim claim) {
        publisher.publishEvent(new ClaimantResponseEvent(claim));
    }

    public void createCCDDefendantResponseEvent(Claim claim, String authorization) {
        publisher.publishEvent(new CCDDefendantResponseEvent(claim, authorization));
    }

    public void createCCDPrePaymentEvent(String externalId, String authorisation) {
        publisher.publishEvent(new CCDPrePaymentEvent(externalId, authorisation));
    }

    public void createCCDClaimIssuedEvent(Claim claim, String authorisation) {
        publisher.publishEvent(new CCDClaimIssuedEvent(claim, authorisation));
    }

    public void createMoreTimeForCCDResponseRequestedEvent(
        String authorisation,
        String externalId,
        LocalDate newDeadline
    ) {
        publisher.publishEvent(new CCDMoreTimeRequestedEvent(authorisation, externalId, newDeadline));
    }

    public void createCCDCountyCourtJudgmentEvent(
        Claim claim,
        String authorisation,
        CountyCourtJudgment countyCourtJudgment,
        boolean issue
    ) {
        publisher.publishEvent(new CCDCountyCourtJudgmentEvent(authorisation, claim,countyCourtJudgment, issue));
    }

    public void linkSealedClaimDocumentCCDEvent(String authorisation, Claim claim, URI sealedClaimDocument) {
        publisher.publishEvent(new CCDLinkSealedClaimDocumentEvent(authorisation, claim, sealedClaimDocument));

    }

    public void linkDefendantCCDEvent(String authorisation) {
        publisher.publishEvent(new CCDLinkDefendantEvent(authorisation));

    }

    public void createCCDClaimantResponseEvent(Claim claim, ClaimantResponse response, String authorization) {
        publisher.publishEvent(new CCDClaimantResponseEvent(claim, response, authorization));
    }

    public void createCCDSettlementEvent(
        Claim claim,
                                                      Settlement settlement,
        String authorization,
        String userAction
    ) {
        publisher.publishEvent(new CCDSettlementEvent(claim, settlement, authorization, userAction));
    }
}
