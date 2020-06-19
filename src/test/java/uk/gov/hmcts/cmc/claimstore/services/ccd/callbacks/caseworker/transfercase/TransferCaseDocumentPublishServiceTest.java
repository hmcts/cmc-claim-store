package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_COURT;

@ExtendWith(MockitoExtension.class)
class TransferCaseDocumentPublishServiceTest {

    private static final String DEFENDANT_ID = "4";
    private static final String AUTHORISATION = "Bearer:auth_token";
    private static final String COURT_LETTER_TEMPLATE_ID = "courtLetterTemplateId";
    private static final String DEFENDANT_LETTER_TEMPLATE_ID = "defendantLetterTemplateId";

    @InjectMocks
    private TransferCaseDocumentPublishService transferCaseDocumentPublishService;

    @Mock
    private TransferCaseLetterSender transferCaseLetterSender;

    @Mock
    private TransferCaseDocumentService transferCaseDocumentService;

    @Mock
    private Claim claim;

    @Mock
    private CoverLetterGenerator coverLetterGenerator;

    private CCDCase ccdCase;

    @BeforeEach
    public void beforeEach() throws IllegalAccessException {

        ccdCase = SampleData.getCCDLegalCase();

        FieldUtils.writeField(transferCaseDocumentPublishService, "courtLetterTemplateId",
            COURT_LETTER_TEMPLATE_ID, true);
        FieldUtils.writeField(transferCaseDocumentPublishService, "defendantLetterTemplateId",
            DEFENDANT_LETTER_TEMPLATE_ID, true);
    }

    @Test
    void shouldPublishNoticesOfTransferToCaseForLinkedDefendant() {

        givenDefendantIsLinked(true);

        CCDDocument generatedCoverDoc = CCDDocument.builder().build();

        CCDDocument namedCoverDoc = generatedCoverDoc.toBuilder()
            .documentFileName(ccdCase.getPreviousServiceCaseReference() + "-notice-of-transfer-for-court.pdf").build();

        when(coverLetterGenerator.generate(ccdCase, AUTHORISATION, FOR_COURT, COURT_LETTER_TEMPLATE_ID))
            .thenReturn(namedCoverDoc);

        when(transferCaseDocumentService.attachNoticeOfTransfer(ccdCase, namedCoverDoc,
            AUTHORISATION)).thenReturn(ccdCase);

        CCDCase returnedCase = transferCaseDocumentPublishService.publishCaseDocuments(ccdCase, AUTHORISATION, claim);

        verify(transferCaseLetterSender).sendAllCaseDocumentsToCourt(AUTHORISATION, ccdCase, claim, namedCoverDoc);

        assertEquals(ccdCase, returnedCase);
    }

    private void givenDefendantIsLinked(boolean isLinked) {

        CCDRespondent.CCDRespondentBuilder defendantBuilder = CCDRespondent.builder();

        if (isLinked) {
            defendantBuilder.defendantId(DEFENDANT_ID);
        }

        List<CCDCollectionElement<CCDRespondent>> respondents
            = singletonList(CCDCollectionElement.<CCDRespondent>builder().value(
            defendantBuilder.build()).build());

        ccdCase = ccdCase.toBuilder().respondents(respondents)
            .transferContent(CCDTransferContent.builder().build())
            .build();
    }
}
