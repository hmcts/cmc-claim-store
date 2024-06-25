package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CCJ_REQUEST_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFENDANT_RESPONSE_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REVIEW_ORDER_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SEALED_CLAIM_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SETTLEMENT_AGREEMENT_UPLOAD;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CCJ_REQUEST;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.REVIEW_ORDER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;

@ExtendWith(SpringExtension.class)
public class CaseEventMapperTest {

    @Test
    public void testSealedClaimCaseEvent() {
        assertEquals(SEALED_CLAIM_UPLOAD, CaseEventMapper.map(SEALED_CLAIM));
    }

    @Test
    public void testClaimIssueReceiptCaseEvent() {
        assertEquals(CLAIM_ISSUE_RECEIPT_UPLOAD, CaseEventMapper.map(CLAIM_ISSUE_RECEIPT));
    }

    @Test
    public void testReviewOrderUploadCaseEvent() {
        assertEquals(REVIEW_ORDER_UPLOAD, CaseEventMapper.map(REVIEW_ORDER));
    }

    @Test
    public void testDefendantResponseReceiptCaseEvent() {
        assertEquals(DEFENDANT_RESPONSE_UPLOAD, CaseEventMapper.map(DEFENDANT_RESPONSE_RECEIPT));
    }

    @Test
    public void testSettlementAgreementCaseEvent() {
        assertEquals(SETTLEMENT_AGREEMENT_UPLOAD, CaseEventMapper.map(SETTLEMENT_AGREEMENT));
    }

    @Test
    public void testCCJUploadCaseEvent() {
        assertEquals(CCJ_REQUEST_UPLOAD, CaseEventMapper.map(CCJ_REQUEST));
    }
}
