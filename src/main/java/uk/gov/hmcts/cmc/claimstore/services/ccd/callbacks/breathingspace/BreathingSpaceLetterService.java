package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType;
import uk.gov.hmcts.cmc.claimstore.documents.PrintService;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.claimstore.services.document.SecuredDocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDataExtractorUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.List;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ADD_BULK_PRINT_DETAILS;
import static uk.gov.hmcts.cmc.claimstore.utils.CaseDataExtractorUtils.getDefendant;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.GENERAL_LETTER;

@Service
public class BreathingSpaceLetterService {
    private final DocAssemblyService docAssemblyService;
    private final DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    private final PrintableDocumentService printableDocumentService;
    private final PrintService bulkPrintService;
    private final ClaimService claimService;
    private final SecuredDocumentManagementService securedDocumentManagementService;
    private final GeneralLetterService generalLetterService;
    private final String caseTypeId;
    private final String jurisdictionId;

    public BreathingSpaceLetterService(
        DocAssemblyService docAssemblyService,
        DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper,
        PrintableDocumentService printableDocumentService,
        PrintService bulkPrintService,
        ClaimService claimService,
        SecuredDocumentManagementService securedDocumentManagementService,
        GeneralLetterService generalLetterService,
        @Value("${ocmc.caseTypeId}") String caseTypeId,
        @Value("${ocmc.jurisdictionId}") String jurisdictionId
    ) {
        this.docAssemblyService = docAssemblyService;
        this.docAssemblyTemplateBodyMapper = docAssemblyTemplateBodyMapper;
        this.printableDocumentService = printableDocumentService;
        this.bulkPrintService = bulkPrintService;
        this.claimService = claimService;
        this.securedDocumentManagementService = securedDocumentManagementService;
        this.generalLetterService = generalLetterService;
        this.caseTypeId = caseTypeId;
        this.jurisdictionId = jurisdictionId;
    }

    public void sendLetterToDefendant(CCDCase ccdCase, Claim claim, String authorisation, String letterTemplateId,
                                      String fileName) {
        String letter = generateLetter(ccdCase, authorisation, letterTemplateId);
        CCDDocument letterDoc = CCDDocument.builder().documentUrl(letter)
            .documentFileName(fileName)
            .build();
        publishLetter(claim, authorisation, letterDoc);
    }

    private String generateLetter(CCDCase ccdCase, String authorisation, String generalLetterTemplateId) {
        var docAssemblyResponse = docAssemblyService.renderTemplate(ccdCase, authorisation, generalLetterTemplateId,
            caseTypeId, jurisdictionId, docAssemblyTemplateBodyMapper.breathingSpaceLetter(ccdCase));

        return docAssemblyResponse.getRenditionOutputLocation();
    }

    private void publishLetter(Claim claim, String authorisation, CCDDocument letterDoc) {

        PDF bsLetter = new PDF(letterDoc.getDocumentFileName(),
            printableDocumentService.pdf(letterDoc, authorisation), GENERAL_LETTER);
        Document bsLetterDoc = printableDocumentService.process(letterDoc, authorisation);
        BulkPrintDetails bulkPrintDetails = bulkPrintService.printPdf(
            claim,
            List.of(
                new PrintableTemplate(
                    bsLetterDoc,
                    buildDefendantLetterFileBaseName(claim.getReferenceNumber()))),
            BulkPrintRequestType.GENERAL_LETTER_TYPE,
            authorisation,
            CaseDataExtractorUtils.getDefendant(claim)
        );

        ImmutableList<BulkPrintDetails> printDetails = ImmutableList.<BulkPrintDetails>builder()
            .addAll(claim.getBulkPrintDetails())
            .add(bulkPrintDetails)
            .build();
        claimService.addBulkPrintDetails(authorisation, printDetails, ADD_BULK_PRINT_DETAILS, claim);
        uploadToDocumentManagement(bsLetter, authorisation, claim);

    }

    public Claim uploadToDocumentManagement(PDF document, String authorisation, Claim claim) {
        ClaimDocument claimDocument = securedDocumentManagementService.uploadDocument(authorisation, document);
        ClaimDocumentCollection claimDocumentCollection = getClaimDocumentCollection(claim, claimDocument);

        return claimService.saveClaimDocuments(authorisation,
            claim.getId(),
            claimDocumentCollection,
            document.getClaimDocumentType());
    }

    private ClaimDocumentCollection getClaimDocumentCollection(Claim claim, ClaimDocument claimDocument) {
        ClaimDocumentCollection claimDocumentCollection = claim.getClaimDocumentCollection()
            .orElse(new ClaimDocumentCollection());
        claimDocumentCollection.addClaimDocument(claimDocument);
        return claimDocumentCollection;
    }

    private CCDCase publishLetterFromCCD(CCDCase ccdCase, Claim claim, String authorisation, CCDDocument letterDoc, List<String> userList) {
        return generalLetterService.publishLetter(ccdCase.toBuilder().draftLetterDoc(letterDoc).build(),
            claim, authorisation,
            letterDoc.getDocumentFileName(),
            userList);
    }

    public CCDCase sendLetterToDefendantFomCCD(CCDCase ccdCase, Claim claim, String authorisation,
                                               String letterTemplateId, String fileName) {
        String letter = generateLetter(ccdCase, authorisation, letterTemplateId);
        CCDDocument letterDoc = CCDDocument.builder().documentUrl(letter)
            .documentFileName(fileName)
            .build();
        return publishLetterFromCCD(ccdCase, claim, authorisation, letterDoc, getDefendant(claim));
    }
}
