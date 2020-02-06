package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class MediationSuccessfulRule {
    public static final String STAFF_UPLOAD_MEDIATION_AGREEMENT =
        "Upload Mediation Agreement";
    public static final String STAFF_UPLOAD_TYPE_MEDIATION_AGREEMENT =
            "Document needs to be a Mediation Agreement";
    public static final String STAFF_UPLOAD_PDF_MEDIATION_AGREEMENT =
            "Document needs to be of type PDF";

    public CCDClaimDocument ccdClaimDocument;
    public CCDDocument ccdDocument;

    public List<String> validateMediationAgreementUploadedByCaseworker(CCDCase ccdCase) {
        Objects.requireNonNull(ccdCase, "CCD case object can not be null");

        List<String> validationErrors = new ArrayList<>();

        if ((ccdCase.getStaffUploadedDocuments().isEmpty())
        ) {
            validationErrors.add(STAFF_UPLOAD_MEDIATION_AGREEMENT);
        }
        if ((ccdClaimDocument.getDocumentType() != CCDClaimDocumentType.MEDIATION_AGREEMENT)
        ) {
            validationErrors.add(STAFF_UPLOAD_TYPE_MEDIATION_AGREEMENT);
        }
        if ((FilenameUtils.getExtension(ccdDocument.getDocumentFileName()).contains("pdf"))
        ) {
            validationErrors.add(STAFF_UPLOAD_PDF_MEDIATION_AGREEMENT);
        } else {
            System.out.println(FilenameUtils.getExtension(ccdDocument.getDocumentFileName()));
        }
        return validationErrors;
    }
}
