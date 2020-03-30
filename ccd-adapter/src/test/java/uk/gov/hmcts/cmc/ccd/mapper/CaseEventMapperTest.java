package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.*;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.*;

@RunWith(JUnit4.class)
public class CaseEventMapperTest {

    @Test
    public void testSealedClaimCaseEvent() {
        assertEquals(CaseEventMapper.map(SEALED_CLAIM), SEALED_CLAIM_UPLOAD);
    }

    @Test
    public void testClaimIssueReceiptCaseEvent() {
        assertEquals(CaseEventMapper.map(CLAIM_ISSUE_RECEIPT), CLAIM_ISSUE_RECEIPT_UPLOAD);
    }

    @Test
    public void testReviewOrderUploadCaseEvent() {
        assertEquals(CaseEventMapper.map(REVIEW_ORDER), REVIEW_ORDER_UPLOAD);
    }

    @Test
    public void testDefendantResponseReceiptCaseEvent() {
        assertEquals(CaseEventMapper.map(DEFENDANT_RESPONSE_RECEIPT), DEFENDANT_RESPONSE_UPLOAD);
    }

    @Test
    public void testSettlementAgreementCaseEvent() {
        assertEquals(CaseEventMapper.map(SETTLEMENT_AGREEMENT), SETTLEMENT_AGREEMENT_UPLOAD);
    }

    @Test
    public void testCCJUploadCaseEvent() {
        assertEquals(CaseEventMapper.map(CCJ_REQUEST), CCJ_REQUEST_UPLOAD);
    }

}
