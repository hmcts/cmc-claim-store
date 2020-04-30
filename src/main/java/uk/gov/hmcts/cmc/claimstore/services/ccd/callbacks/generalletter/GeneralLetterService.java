package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
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
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterCallbackHandler.DRAFT_LETTER_DOC;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class GeneralLetterService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CaseDetailsConverter caseDetailsConverter;
    private final DocAssemblyService docAssemblyService;
    private final ApplicationEventPublisher publisher;
    private final DocumentManagementService documentManagementService;
    private final Clock clock;
    private final UserService userService;
    private final String generalLetterTemplateId;

    public GeneralLetterService(
        CaseDetailsConverter caseDetailsConverter,
        DocAssemblyService docAssemblyService,
        ApplicationEventPublisher publisher,
        DocumentManagementService documentManagementService,
        Clock clock,
        UserService userService,
        @Value("${doc_assembly.generalLetterTemplateId}") String generalLetterTemplateId
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.docAssemblyService = docAssemblyService;
        this.publisher = publisher;
        this.documentManagementService = documentManagementService;
        this.clock = clock;
        this.userService = userService;
        this.generalLetterTemplateId = generalLetterTemplateId;
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

    public String createAndPreview(CCDCase ccdCase,
                                   String authorisation,
                                   String templateId) throws DocumentGenerationFailedException {
        logger.info("General Letter: creating general letter");

        CaseDetails caseDetails = CaseDetails.builder().data(caseDetailsConverter.convertToMap(ccdCase)).build();
        caseDetails.getData().remove(DRAFT_LETTER_DOC);
        DocAssemblyResponse docAssemblyResponse = docAssemblyService.createGeneralLetter(ccdCase,
            authorisation, templateId);
        return docAssemblyResponse.getRenditionOutputLocation();
    }

    public CCDCase printAndUpdateCaseDocuments(CCDCase ccdCase,
                                               Claim claim,
                                               String authorisation,
                                               String documentName,
                                               String docUrl) {
        CCDDocument draftLetterDoc;
        if (ccdCase.getDraftLetterDoc() == null) {
            draftLetterDoc = CCDDocument.builder()
                .documentUrl(docUrl)
                .documentBinaryUrl(docUrl + "/binary")
                .documentFileName("")
                .build();
        } else {
            draftLetterDoc = ccdCase.getDraftLetterDoc();
        }

        try {
            printLetter(authorisation, draftLetterDoc, claim);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return ccdCase.toBuilder()
            .caseDocuments(updateCaseDocumentsWithGeneralLetter(ccdCase, draftLetterDoc, documentName))
            .draftLetterDoc(null)
            .contactChangeParty(null)
            .contactChangeContent(null)
            .generalLetterContent(null)
            .build();
    }

    private List<CCDCollectionElement<CCDClaimDocument>> updateCaseDocumentsWithGeneralLetter(
        CCDCase ccdCase,
        CCDDocument draftLetterDoc,
        String documentName) {

        CCDCollectionElement<CCDClaimDocument> claimDocument = CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(CCDDocument.builder()
                    .documentFileName(documentName)
                    .documentUrl(draftLetterDoc.getDocumentUrl())
                    .documentBinaryUrl(draftLetterDoc.getDocumentBinaryUrl())
                    .build())
                .documentName(documentName)
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

    private CCDCase setLetterContent(CCDCase ccdCase,
                                     String content,
                                     UserDetails userDetails,
                                     CCDContactPartyType ccdContactPartyType) {

        String caseworkerName = userDetails.getFullName();
        GeneralLetterContent generalLetterContent = GeneralLetterContent.builder()
            .caseworkerName(caseworkerName)
            .letterContent(content)
            .issueLetterContact(ccdContactPartyType)
            .build();

        return ccdCase.toBuilder()
            .generalLetterContent(generalLetterContent)
            .build();
    }

    public CCDCase createAndPrintLetter(CCDCase ccdCase,
                                        Claim claim,
                                        String authorisation,
                                        String content,
                                        String filename,
                                        CCDContactPartyType partyType) {

        UserDetails userDetails = userService.getUserDetails(authorisation);
        CCDCase updatedCCDCase = setLetterContent(ccdCase, content, userDetails, partyType);
        String docUrl = createAndPreview(updatedCCDCase, authorisation, generalLetterTemplateId);

        return printAndUpdateCaseDocuments(updatedCCDCase, claim, authorisation, filename, docUrl);
    }
}
