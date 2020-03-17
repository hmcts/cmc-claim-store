package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.events.GeneralLetterReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class GeneralLetterService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String ERROR_MESSAGE =
        "There was a technical problem. Nothing has been sent. You need to try again.";

    private final CaseDetailsConverter caseDetailsConverter;
    private final DocAssemblyService docAssemblyService;
    private final ApplicationEventPublisher publisher;
    private final DocumentManagementService documentManagementService;
    private final Clock clock;

    public GeneralLetterService(
        CaseDetailsConverter caseDetailsConverter,
        DocAssemblyService docAssemblyService,
        ApplicationEventPublisher publisher,
        DocumentManagementService documentManagementService,
        Clock clock
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.docAssemblyService = docAssemblyService;
        this.publisher = publisher;
        this.documentManagementService = documentManagementService;
        this.clock = clock;
    }

    public CallbackResponse createAndPreview(CaseDetails caseDetails, String authorisation, String letterType) {
        try {
            logger.info("General Letter: creating letter");
            CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
            DocAssemblyResponse docAssemblyResponse = docAssemblyService.createGeneralLetter(ccdCase, authorisation);
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(ImmutableMap.of(
                    letterType,
                    CCDDocument.builder().documentUrl(docAssemblyResponse.getRenditionOutputLocation())))
                .build();
        } catch (Exception e) {
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(Collections.singletonList(ERROR_MESSAGE))
                .build();
        }
    }

    public CallbackResponse printAndUpdateCaseDocuments(CaseDetails caseDetails, String authorisation) {
        try {
            CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
            CCDDocument draftLetterDoc = ccdCase.getDraftLetterDoc();
            Claim claim = caseDetailsConverter.extractClaim(caseDetails);
            printLetter(authorisation, draftLetterDoc, claim);
            CCDCase updatedCase = ccdCase.toBuilder()
                .caseDocuments(updateCaseDocumentsWithOrder(ccdCase, draftLetterDoc))
                .build();
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(caseDetailsConverter.convertToMap(updatedCase))
                .build();

        } catch (Exception e) {
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(Collections.singletonList(ERROR_MESSAGE))
                .build();
        }
    }

    private List<CCDCollectionElement<CCDClaimDocument>> updateCaseDocumentsWithOrder(
        CCDCase ccdCase,
        CCDDocument draftLetterDoc
    ) {
        CCDCollectionElement<CCDClaimDocument> claimDocument = CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(draftLetterDoc)
                .documentName(draftLetterDoc.getDocumentFileName())
                .createdDatetime(LocalDateTime.now(clock.withZone(UTC_ZONE)))
                .documentType(GENERAL_LETTER)
                .build())
            .build();
        return ImmutableList.<CCDCollectionElement<CCDClaimDocument>>builder()
            .addAll(ccdCase.getCaseDocuments())
            .add(claimDocument)
            .build();
    }

    private void printLetter(String authorisation, CCDDocument document, Claim claim) throws URISyntaxException {
        GeneralLetterReadyToPrintEvent event = new GeneralLetterReadyToPrintEvent(
            claim,
            downloadLetter(authorisation, document)
        );
        publisher.publishEvent(event);
    }

    private Document downloadLetter(String authorisation, CCDDocument document) throws URISyntaxException {
        return new Document(Base64.getEncoder().encodeToString(
            documentManagementService.downloadDocument(
                authorisation,
                ClaimDocument.builder()
                    .documentName(document.getDocumentFileName())
                    .documentManagementUrl(new URI(document.getDocumentUrl()))
                    .documentManagementBinaryUrl(new URI(document.getDocumentBinaryUrl()))
                    .build())),
            Collections.emptyMap());
    }
}
