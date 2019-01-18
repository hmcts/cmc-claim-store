package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDClaimantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDCountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDDefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDInterlocutoryJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDLinkDefendantEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDLinkSealedClaimDocumentEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDMoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDPaidInFullEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDPrePaymentEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDRejectOrganisationPaymentPlanEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDSettlementEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDTestingLinkDefendantEvent;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDTestingResponseDeadlineEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;

import java.net.URI;
import java.time.LocalDate;

@Component
public class CCDEventProducer {
    private final ApplicationEventPublisher publisher;

    public CCDEventProducer(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
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
        CountyCourtJudgment countyCourtJudgment
    ) {
        publisher.publishEvent(new CCDCountyCourtJudgmentEvent(authorisation, claim, countyCourtJudgment));
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

    public void createCCDResponseDeadlineEvent(
        String claimReferenceNumber,
        String authorisation,
        LocalDate newDeadline
    ) {
        publisher.publishEvent(new CCDTestingResponseDeadlineEvent(claimReferenceNumber, authorisation, newDeadline));
    }

    public void createCCDLinkDefendantEvent(String claimReferenceNumber, String defendantId) {
        publisher.publishEvent(new CCDTestingLinkDefendantEvent(claimReferenceNumber, defendantId));
    }

    public void createCCDRejectOrganisationPaymentPlanEvent(Claim claim, String authorisation) {
        publisher.publishEvent(new CCDRejectOrganisationPaymentPlanEvent(claim, authorisation));
    }

    public void createCCDInterlocutoryJudgmentEvent(Claim claim, String authorisation) {
        publisher.publishEvent(new CCDInterlocutoryJudgmentEvent(claim, authorisation));
    }

    public void createCCDPaidInFullEvent(String authorisation, Claim claim, PaidInFull paidInFull) {
        publisher.publishEvent(new CCDPaidInFullEvent(authorisation, claim, paidInFull));
    }
}
