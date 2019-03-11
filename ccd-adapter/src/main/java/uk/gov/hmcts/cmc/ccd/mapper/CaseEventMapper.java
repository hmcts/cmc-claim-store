package uk.gov.hmcts.cmc.ccd.mapper;

import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.*;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.*;

public class CaseEventMapper {

    private static final Map<ClaimDocumentType, CaseEvent> claimDocumentTypeMap;

    static {
        claimDocumentTypeMap = new HashMap<>();
        claimDocumentTypeMap.put(SEALED_CLAIM, SEALED_CLAIM_UPLOAD);
        claimDocumentTypeMap.put(CLAIM_ISSUE_RECEIPT, CLAIM_ISSUE_RECEIPT_UPLOAD);
        claimDocumentTypeMap.put(DEFENDANT_RESPONSE_RECEIPT, DEFENDANT_RESPONSE_UPLOAD);
        claimDocumentTypeMap.put(CCJ_REQUEST, CCJ_REQUEST_UPLOAD);
        claimDocumentTypeMap.put(SETTLEMENT_AGREEMENT, SETTLEMENT_AGREEMENT_UPLOAD);
        claimDocumentTypeMap.put(DEFENDANT_PIN_LETTER, DEFENDANT_PIN_LETTER_UPLOAD);
    }

    public static CaseEvent map(ClaimDocumentType type) {

        return claimDocumentTypeMap.getOrDefault(type, LINK_SEALED_CLAIM);
    }
}
