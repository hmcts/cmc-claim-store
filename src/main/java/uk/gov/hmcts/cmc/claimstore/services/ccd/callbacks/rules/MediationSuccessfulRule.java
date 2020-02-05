package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class MediationSuccessfulRule {
    public static final String STAFF_UPLOAD_MEDIATION_AGREEMENT =
        "Upload Mediation Agreement";
    public static final String STAFF_UPLOAD_TYPE_MEDIATION_AGREEMENT =
            "Document type has to be Mediation Agreement";
    public static final String STAFF_UPLOAD_PDF_MEDIATION_AGREEMENT =
            "Document needs to be of type PDF";

    CCDClaimDocument ccdClaimDocument;
    CCDClaimDocumentType ccdClaimDocumentType;


    public List<String> validateMediationAgreementUploadedByCaseworker(CCDCase ccdCase) {
        Objects.requireNonNull(ccdCase, "CCD case object can not be null");

        List<String> validationErrors = new ArrayList<>();

        if ((ccdCase.getStaffUploadedDocuments().isEmpty())
        ) {
            validationErrors.add(STAFF_UPLOAD_MEDIATION_AGREEMENT);
        }
        //ccdClaimDocument vs Claim Document?
        if ((ccdClaimDocument.getDocumentType() != ccdClaimDocumentType.MEDIATION_AGREEMENT)
        ) {
            validationErrors.add(STAFF_UPLOAD_TYPE_MEDIATION_AGREEMENT);
        }
//        if ((ccdClaimDocument.getDocumentFileType() != )
//        ) {
//            validationErrors.add(STAFF_UPLOAD_PDF_MEDIATION_AGREEMENT);
//        }
        return validationErrors;
    }

}
