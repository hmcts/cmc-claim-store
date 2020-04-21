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
import uk.gov.hmcts.cmc.ccd.domain.GeneralLetterContent;
import uk.gov.hmcts.cmc.claimstore.events.GeneralLetterReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.exception.DocumentGenerationFailedException;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildLetterFileBaseName;
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
    private final UserService userService;

    public GeneralLetterService(
        CaseDetailsConverter caseDetailsConverter,
        DocAssemblyService docAssemblyService,
        ApplicationEventPublisher publisher,
        DocumentManagementService documentManagementService,
        Clock clock,
        UserService userService
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.docAssemblyService = docAssemblyService;
        this.publisher = publisher;
        this.documentManagementService = documentManagementService;
        this.clock = clock;
        this.userService = userService;
    }

    public CallbackResponse prepopulateData(String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        String caseworkerName = userDetails.getFullName();
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(ImmutableMap.of("generalLetterContent",
                GeneralLetterContent.builder().caseworkerName(caseworkerName).build()
            ))
            .build();
    }

    public CallbackResponse createAndPreview(
        CaseDetails caseDetails,
        String authorisation,
        String letterType,
        String templateId
    ) {
        try {
            logger.info("General Letter: creating general letter");

            CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
            DocAssemblyResponse docAssemblyResponse = docAssemblyService.createGeneralLetter(ccdCase,
                authorisation, templateId);
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(ImmutableMap.of(
                    letterType,
                    CCDDocument.builder().documentUrl(docAssemblyResponse.getRenditionOutputLocation()).build()
                ))
                .build();
        } catch (DocumentGenerationFailedException e) {
            logger.info("General Letter creating and preview failed", e);
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(Collections.singletonList(ERROR_MESSAGE))
                .build();
        }
    }

    public CallbackResponse printAndUpdateCaseDocuments(CaseDetails caseDetails, String authorisation) {

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        CCDDocument draftLetterDoc = ccdCase.getDraftLetterDoc();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        boolean errors = false;
        try {

            printLetter(authorisation, draftLetterDoc, claim);
        } catch (Exception e) {
            logger.info("General Letter printing and case documents update failed", e);
            errors = true;
        }
        if (!errors) {
            logger.info("General Letter: updating case document with general letter");
            CCDCase updatedCase = ccdCase.toBuilder()
                .caseDocuments(updateCaseDocumentsWithGeneralLetter(ccdCase, draftLetterDoc))
                .draftLetterDoc(null)
                .generalLetterContent(null)
                .build();
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(caseDetailsConverter.convertToMap(updatedCase))
                .build();
        } else {
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(Collections.singletonList(ERROR_MESSAGE))
                .build();
        }
    }

    public List<CCDCollectionElement<CCDClaimDocument>> updateCaseDocumentsWithGeneralLetter(
        CCDCase ccdCase,
        CCDDocument draftLetterDoc
    ) {
        String documentName = getDocumentName(ccdCase);
        CCDCollectionElement<CCDClaimDocument> claimDocument = CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(CCDDocument.builder()
                .documentFileName(documentName)
                .documentUrl(draftLetterDoc.getDocumentUrl())
                .documentBinaryUrl(draftLetterDoc.getDocumentBinaryUrl())
                .build())
                .documentName(getDocumentName(ccdCase))
                .createdDatetime(LocalDateTime.now(clock.withZone(UTC_ZONE)))
                .documentType(GENERAL_LETTER)
                .build())
            .build();
        return ImmutableList.<CCDCollectionElement<CCDClaimDocument>>builder()
            .addAll(ccdCase.getCaseDocuments())
            .add(claimDocument)
            .build();
    }

    private String getDocumentName(CCDCase ccdCase) {
        String number = String.valueOf((ccdCase.getCaseDocuments()
            .stream()
            .map(CCDCollectionElement::getValue)
            .filter(c -> c.getDocumentType().equals(GENERAL_LETTER))
            .filter(c -> c.getDocumentName().contains(LocalDate.now().toString()))
            .count() + 1));
        return buildLetterFileBaseName(ccdCase.getPreviousServiceCaseReference(),
            LocalDate.now().toString()) + "-" + number + ".pdf";
    }

    public void printLetter(String authorisation, CCDDocument document, Claim claim) throws URISyntaxException {
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
