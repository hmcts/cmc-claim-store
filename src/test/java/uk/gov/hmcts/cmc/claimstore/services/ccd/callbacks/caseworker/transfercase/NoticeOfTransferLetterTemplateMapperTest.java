package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferReason;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@ExtendWith(MockitoExtension.class)
class NoticeOfTransferLetterTemplateMapperTest {

    private static final String TRANSFER_COURT_NAME = "Bristol";
    private static final String CASE_REFERENCE = "REF1";
    private static final String TRANSFER_REASON = "A reason";
    public static final String TRANSFER_DATE = "2019-04-24";
    private static final String CASE_WORKER_NAME = "John";
    private static final String DEFENDANT_NAME = "Sue";

    @InjectMocks
    private NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper;

    @Mock
    private Clock clock;

    private CCDCase ccdCase;

    @Mock
    private CCDAddress defendantAddress;

    @Mock
    private CCDAddress transferCourtAddress;

    @BeforeEach
    public void beforeEach() {

        when(clock.instant()).thenReturn(LocalDate.parse(TRANSFER_DATE)
            .atStartOfDay().toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.withZone(UTC_ZONE)).thenReturn(clock);

        List<CCDCollectionElement<CCDRespondent>> respondents
            = singletonList(CCDCollectionElement.<CCDRespondent>builder().value(
            CCDRespondent.builder()
                .partyName(DEFENDANT_NAME)
                .claimantProvidedPartyName(DEFENDANT_NAME)
                .claimantProvidedDetail(
                    CCDParty.builder()
                        .primaryAddress(defendantAddress)
                        .build()
                )
                .build()).build());

        ccdCase = CCDCase.builder()
            .previousServiceCaseReference(CASE_REFERENCE)
            .respondents(respondents)
            .hearingCourtName(TRANSFER_COURT_NAME)
            .hearingCourtAddress(transferCourtAddress)
            .transferContent(CCDTransferContent.builder()
                .transferReason(CCDTransferReason.OTHER)
                .transferReasonOther(TRANSFER_REASON)
                .build())
            .build();
    }

    @Test
    void shouldMapNoticeOfTransferLetterBodyForCourt() {

        DocAssemblyTemplateBody requestBody = noticeOfTransferLetterTemplateMapper
            .noticeOfTransferLetterBodyForCourt(ccdCase, CASE_WORKER_NAME);

        DocAssemblyTemplateBody expectedRequestBody = baseExpectedRequestBodyBuilder()
            .partyName(TRANSFER_COURT_NAME)
            .partyAddress(transferCourtAddress)
            .build();

        assertEquals(expectedRequestBody, requestBody);
    }

    @Test
    void shouldMapNoticeOfTransferLetterBodyForDefendant() {

        DocAssemblyTemplateBody requestBody = noticeOfTransferLetterTemplateMapper
            .noticeOfTransferLetterBodyForDefendant(ccdCase, CASE_WORKER_NAME);

        DocAssemblyTemplateBody expectedRequestBody = baseExpectedRequestBodyBuilder()
            .partyName(DEFENDANT_NAME)
            .partyAddress(defendantAddress)
            .build();

        assertEquals(expectedRequestBody, requestBody);
    }

    private DocAssemblyTemplateBody.DocAssemblyTemplateBodyBuilder baseExpectedRequestBodyBuilder() {

        DocAssemblyTemplateBody.DocAssemblyTemplateBodyBuilder bodyBuilder = DocAssemblyTemplateBody
            .builder()
            .referenceNumber(CASE_REFERENCE)
            .currentDate(LocalDate.parse(TRANSFER_DATE))
            .hearingCourtName(TRANSFER_COURT_NAME)
            .hearingCourtAddress(transferCourtAddress)
            .reasonForTransfer(TRANSFER_REASON)
            .caseworkerName(CASE_WORKER_NAME);

        return bodyBuilder;
    }
}
