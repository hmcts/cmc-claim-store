package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;

@Component
public class DocumentUploadHandler {

    private final ApplicationEventPublisher publisher;
    private final DefendantResponseReceiptService defendantResponseReceiptService;

    @Autowired
    public DocumentUploadHandler(ApplicationEventPublisher publisher,
                                 DefendantResponseReceiptService defendantResponseReceiptService) {
        this.publisher = publisher;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
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
