package uk.gov.hmcts.cmc.claimstore.events.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentUploadEvent;
import uk.gov.hmcts.cmc.claimstore.services.notifications.DefendantResponseNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ResponseSubmitted.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ResponseSubmitted.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;

@Component
public class DefendantResponseCitizenNotificationsHandler {
    private final DefendantResponseNotificationService defendantResponseNotificationService;
    private final ApplicationEventPublisher publisher;
    private final DefendantResponseReceiptService defendantResponseReceiptService;

    @Autowired
    public DefendantResponseCitizenNotificationsHandler(
        DefendantResponseNotificationService defendantResponseNotificationService,
        ApplicationEventPublisher publisher,
        DefendantResponseReceiptService defendantResponseReceiptService) {
        this.defendantResponseNotificationService = defendantResponseNotificationService;
        this.publisher = publisher;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
    }

    @EventListener
    public void notifyDefendantResponse(DefendantResponseEvent event) {
        Claim claim = event.getClaim();
        if (isAdmissionResponse(claim)) {
            return;
        }
        defendantResponseNotificationService.notifyDefendant(
            claim,
            claim.getDefendantEmail(),
            referenceForDefendant(claim.getReferenceNumber())
        );
    }

    private boolean isAdmissionResponse(Claim claim) {
        ResponseType responseType = claim.getResponse().orElseThrow(IllegalArgumentException::new).getResponseType();
        return responseType == ResponseType.FULL_ADMISSION || responseType == ResponseType.PART_ADMISSION;
    }

    @EventListener
    public void notifyClaimantResponse(DefendantResponseEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, "Claim must be present");
        if (!claim.getResponse().isPresent()) {
            throw new IllegalArgumentException("Response must be present");
        }
        defendantResponseNotificationService.notifyClaimant(
            claim,
            referenceForClaimant(claim.getReferenceNumber())
        );
    }

    @EventListener
    public void uploadDocument(DefendantResponseEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, "Claim must be present");
        if (!claim.getResponse().isPresent()) {
            throw new IllegalArgumentException("Response must be present");
        }
        PDF defendantResponseDocument = new PDF(buildResponseFileBaseName(claim.getReferenceNumber()),
            defendantResponseReceiptService.createPdf(claim),
            DEFENDANT_RESPONSE_RECEIPT);
        publisher.publishEvent(new DocumentUploadEvent(claim,
            event.getAuthorization(),
            defendantResponseDocument));
    }
}
