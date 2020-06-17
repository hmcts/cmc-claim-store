package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;

@Service
public class TransferCaseDocumentService {

    private final GeneralLetterService generalLetterService;

    public TransferCaseDocumentService(GeneralLetterService generalLetterService) {
        this.generalLetterService = generalLetterService;
    }

    public CCDCase attachNoticeOfTransfer(CCDCase ccdCase, CCDDocument ccdDocument, String authorisation) {

        return generalLetterService.attachGeneralLetterToCase(ccdCase,
            ccdDocument,
            ccdDocument.getDocumentFileName(),
            authorisation
        );
    }
}
