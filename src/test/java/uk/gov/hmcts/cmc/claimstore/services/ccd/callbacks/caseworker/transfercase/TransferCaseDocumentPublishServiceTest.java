package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDBulkPrintDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.mapper.BulkPrintDetailsMapper;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.bulkprint.PrintRequestType.BULK_PRINT_TRANSFER;

@ExtendWith(MockitoExtension.class)
class TransferCaseDocumentPublishServiceTest {

    private static final String DEFENDANT_ID = "4";
    private static final String AUTHORISATION = "Bearer:auth_token";
    private static final String COURT_LETTER_TEMPLATE_ID = "courtLetterTemplateId";
    private static final String DEFENDANT_LETTER_TEMPLATE_ID = "defendantLetterTemplateId";

    @InjectMocks
    private TransferCaseDocumentPublishService transferCaseDocumentPublishService;

    @Spy
    private BulkPrintDetailsMapper bulkPrintDetailsMapper = new BulkPrintDetailsMapper();

    @Mock
    private TransferCaseLetterSender transferCaseLetterSender;

    @Mock
    private TransferCaseDocumentService transferCaseDocumentService;

    @Mock
    private DocAssemblyService docAssemblyService;

    @Mock
    private NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper;

    @Mock
    private Claim claim;

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
        DocAssemblyTemplateBody formPayloadForCourt = mock(DocAssemblyTemplateBody.class);

        when(noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForCourt(eq(ccdCase), eq(AUTHORISATION)))
            .thenReturn(formPayloadForCourt);

        when(docAssemblyService
            .generateDocument(eq(AUTHORISATION), eq(formPayloadForCourt), eq(COURT_LETTER_TEMPLATE_ID)))
            .thenReturn(generatedCoverDoc);

        CCDDocument namedCoverDoc = generatedCoverDoc.toBuilder()
            .documentFileName(ccdCase.getPreviousServiceCaseReference() + "-notice-of-transfer-for-court.pdf").build();

        when(transferCaseDocumentService.attachNoticeOfTransfer(eq(ccdCase), eq(namedCoverDoc), eq(AUTHORISATION)))
            .thenReturn(ccdCase);

        BulkPrintDetails bulkPrintDetails = BulkPrintDetails.builder().printRequestType(BULK_PRINT_TRANSFER)
            .printRequestId("requestId")
            .build();
        when(transferCaseLetterSender
            .sendAllCaseDocumentsToCourt(eq(AUTHORISATION), eq(ccdCase), eq(claim), eq(namedCoverDoc)))
            .thenReturn(bulkPrintDetails);

        CCDCase returnedCase = transferCaseDocumentPublishService.publishCaseDocuments(ccdCase, AUTHORISATION, claim);

        verify(transferCaseLetterSender)
            .sendAllCaseDocumentsToCourt(eq(AUTHORISATION), eq(ccdCase), eq(claim), eq(namedCoverDoc));

        assertEquals(addBulkPrintDeatils(ccdCase, bulkPrintDetails), returnedCase);
    }

    private CCDCase addBulkPrintDeatils(CCDCase ccdCase, BulkPrintDetails input) {
        ImmutableList.Builder<CCDCollectionElement<CCDBulkPrintDetails>> printDetails = ImmutableList.builder();
        printDetails.addAll(ccdCase.getBulkPrintDetails());
        printDetails.add(bulkPrintDetailsMapper.to(input));

        return ccdCase.toBuilder().bulkPrintDetails(printDetails.build()).build();
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
