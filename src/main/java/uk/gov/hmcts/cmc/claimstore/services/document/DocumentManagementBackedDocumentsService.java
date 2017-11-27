package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.function.Supplier;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFilename;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "true")
public class DocumentManagementBackedDocumentsService implements DocumentsService {

    private final ClaimService claimService;
    private final DocumentManagementService documentManagementService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final DefendantResponseCopyService defendantResponseCopyService;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;

    @Autowired
    public DocumentManagementBackedDocumentsService(
        final ClaimService claimService,
        final DocumentManagementService documentManagementService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final DefendantResponseCopyService defendantResponseCopyService,
        final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService,
        final SettlementAgreementCopyService settlementAgreementCopyService
    ) {
        this.claimService = claimService;
        this.documentManagementService = documentManagementService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.defendantResponseCopyService = defendantResponseCopyService;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
    }

    @Override
    public byte[] getLegalSealedClaim(final String externalId, final String authorisation) {
        Claim claim = getClaimByExternalId(externalId);
        return downloadOrGenerateAndUpload(claim, () -> legalSealedClaimPdfService.createPdf(claim), authorisation);
    }

    @Override
    public byte[] generateDefendantResponseCopy(final String externalId) {
        return defendantResponseCopyService.createPdf(getClaimByExternalId(externalId));
    }

    @Override
    public byte[] generateCountyCourtJudgement(final String externalId) {
        return countyCourtJudgmentPdfService.createPdf(getClaimByExternalId(externalId));
    }

    @Override
    public byte[] generateSettlementAgreement(final String externalId) {
        return settlementAgreementCopyService.createPdf(getClaimByExternalId(externalId));
    }

    private Claim getClaimByExternalId(final String externalId) {
        return claimService.getClaimByExternalId(externalId);
    }

    private byte[] downloadOrGenerateAndUpload(Claim claim, Supplier<byte[]> documentSupplier, String authorisation) {
        if (claim.getSealedClaimDocumentSelfPath().isPresent()) {
            String documentSelfPath = claim.getSealedClaimDocumentSelfPath().get();
            return documentManagementService.downloadDocument(authorisation, documentSelfPath);
        } else {
            byte[] document = documentSupplier.get();

            String documentSelfPath = documentManagementService.uploadDocument(authorisation,
                buildSealedClaimFilename(claim.getReferenceNumber()) + PDF.EXTENSION, document,
                PDF.CONTENT_TYPE);
            claimService.linkSealedClaimDocument(claim.getId(), documentSelfPath);

            return document;
        }
    }
}
