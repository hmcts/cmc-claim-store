package uk.gov.hmcts.cmc.ccd.mapper;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CLAIMANT_DIRECTIONS_QUESTIONNAIRE_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CLAIMANT_RESPONSE_RECEIPT_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REDETERMINATION_RECEIPT_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFENDANT_RESPONSE_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LINK_SEALED_CLAIM;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REVIEW_ORDER_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SEALED_CLAIM_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SETTLEMENT_AGREEMENT_UPLOAD;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.*;

public class CaseEventMapper {

    private static final ImmutableMap<ClaimDocumentType, CaseEvent> claimDocumentTypeMap =
        ImmutableMap.<ClaimDocumentType, CaseEvent>builder()
            .put(SEALED_CLAIM, SEALED_CLAIM_UPLOAD)
            .put(CLAIM_ISSUE_RECEIPT, CLAIM_ISSUE_RECEIPT_UPLOAD)
            .put(DEFENDANT_RESPONSE_RECEIPT, DEFENDANT_RESPONSE_UPLOAD)
            .put(SETTLEMENT_AGREEMENT, SETTLEMENT_AGREEMENT_UPLOAD)
            .put(CLAIMANT_DIRECTIONS_QUESTIONNAIRE, CLAIMANT_DIRECTIONS_QUESTIONNAIRE_UPLOAD)
            .put(REVIEW_ORDER, REVIEW_ORDER_UPLOAD)
            .put(REDETERMINATION_RECEIPT, REDETERMINATION_RECEIPT_UPLOAD)
            .put(CLAIMANT_RESPONSE_RECEIPT, CLAIMANT_RESPONSE_RECEIPT_UPLOAD)
            .build();

    private CaseEventMapper() {

    }

    public static CaseEvent map(ClaimDocumentType type) {

        return claimDocumentTypeMap.getOrDefault(type, LINK_SEALED_CLAIM);
    }
}
