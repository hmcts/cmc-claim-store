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
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferReason;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.domain.models.ClaimFeatures;

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

    public static final String LISTING = "Listing";
    private static final String TRANSFER_COURT_NAME = "Bristol";
    private static final String CASE_REFERENCE = "REF1";
    private static final String TRANSFER_REASON = "A reason";
    public static final String TRANSFER_DATE = "2019-04-24";
    private static final String CASE_WORKER_NAME = "John";
    private static final String DEFENDANT_NAME = "Sue";
    private static final String AUTHORISATION = "Bearer:auth_token";

    @InjectMocks
    private NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper;

    @Mock
    private Clock clock;

    private CCDCase ccdCase;

    @Mock
    private CCDAddress defendantAddress;

    @Mock
    private CCDAddress transferCourtAddress;

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    public void beforeEach() {

        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getFullName()).thenReturn(CASE_WORKER_NAME);

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
            .transferContent(CCDTransferContent.builder()
                .transferReason(CCDTransferReason.OTHER)
                .transferCourtName(TRANSFER_COURT_NAME)
                .transferCourtAddress(transferCourtAddress)
                .transferReasonOther(TRANSFER_REASON)
                .build())
            .build();
    }

    @Test
    void shouldMapNoticeOfTransferLetterBodyForCourtWithNoOrder() {

        DocAssemblyTemplateBody requestBody = noticeOfTransferLetterTemplateMapper
            .noticeOfTransferLetterBodyForCourt(ccdCase, AUTHORISATION);

        DocAssemblyTemplateBody expectedRequestBody = baseExpectedRequestBodyBuilder().build();

        assertEquals(expectedRequestBody, requestBody);
    }

    @Test
    void shouldMapNoticeOfTransferLetterBodyForCourtWithJudgeOrder() {

        ccdCase = ccdCase.toBuilder()
            .directionOrder(CCDDirectionOrder.builder().build())
            .features(ClaimFeatures.JUDGE_PILOT_FLAG.getValue())
            .build();

        DocAssemblyTemplateBody requestBody = noticeOfTransferLetterTemplateMapper
            .noticeOfTransferLetterBodyForCourt(ccdCase, AUTHORISATION);

        DocAssemblyTemplateBody expectedRequestBody = baseExpectedRequestBodyBuilder()
            .orderDrawnByJudge(true)
            .build();

        assertEquals(expectedRequestBody, requestBody);
    }

    @Test
    void shouldMapNoticeOfTransferLetterBodyForCourtWithLAOrder() {

        ccdCase = ccdCase.toBuilder()
            .directionOrder(CCDDirectionOrder.builder().build())
            .features(ClaimFeatures.LA_PILOT_FLAG.getValue())
            .build();

        DocAssemblyTemplateBody requestBody = noticeOfTransferLetterTemplateMapper
            .noticeOfTransferLetterBodyForCourt(ccdCase, AUTHORISATION);

        DocAssemblyTemplateBody expectedRequestBody = baseExpectedRequestBodyBuilder()
            .orderDrawnByLA(true)
            .build();

        assertEquals(expectedRequestBody, requestBody);
    }

    @Test
    void shouldMapNoticeOfTransferLetterBodyForListing() {

        ccdCase = CCDCase.builder()
            .previousServiceCaseReference(CASE_REFERENCE)
            .transferContent(CCDTransferContent.builder()
                .transferReason(CCDTransferReason.OTHER)
                .transferCourtName(TRANSFER_COURT_NAME)
                .transferCourtAddress(transferCourtAddress)
                .transferReason(CCDTransferReason.LISTING)
                .build())
            .build();
        DocAssemblyTemplateBody requestBody = noticeOfTransferLetterTemplateMapper
            .noticeOfTransferLetterBodyForCourt(ccdCase, AUTHORISATION);

        DocAssemblyTemplateBody expectedRequestBody = listingRequestBodyBuilder()
            .build();

        assertEquals(expectedRequestBody, requestBody);
    }

    @Test
    void shouldMapNoticeOfTransferLetterBodyForDefendant() {

        DocAssemblyTemplateBody requestBody = noticeOfTransferLetterTemplateMapper
            .noticeOfTransferLetterBodyForDefendant(ccdCase, AUTHORISATION);

        DocAssemblyTemplateBody expectedRequestBody = baseExpectedRequestBodyBuilder()
            .partyName(DEFENDANT_NAME)
            .partyAddress(defendantAddress)
            .build();

        assertEquals(expectedRequestBody, requestBody);
    }

    @Test
    void shouldMapNoticeOfTransferLetterWhenDefendantNotLinkedButChangedContactDetailsViaCCD() {

        ccdCase = CCDCase.builder()
            .previousServiceCaseReference(CASE_REFERENCE)
            .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(CCDRespondent.builder()
                    .partyName(DEFENDANT_NAME)
                    .claimantProvidedPartyName(DEFENDANT_NAME)
                    .partyDetail(CCDParty.builder()
                        .primaryAddress(CCDAddress.builder().addressLine1("NEW ADDRESS1")
                            .addressLine2("NEW ADDRESS2")
                            .postCode("NEW POSTCODE").build())
                        .build())
                    .claimantProvidedDetail(CCDParty.builder()
                        .primaryAddress(CCDAddress.builder().addressLine1("OLD ADDRESS1")
                            .addressLine2("OLD ADDRESS2")
                            .postCode("OLD POSTCODE").build())
                        .build())
                    .build())
                .build()))
            .transferContent(CCDTransferContent.builder()
                .transferReason(CCDTransferReason.OTHER)
                .transferCourtName(TRANSFER_COURT_NAME)
                .transferCourtAddress(transferCourtAddress)
                .transferReasonOther(TRANSFER_REASON)
                .build())
            .build();

        DocAssemblyTemplateBody requestBody = noticeOfTransferLetterTemplateMapper
            .noticeOfTransferLetterBodyForDefendant(ccdCase, AUTHORISATION);

        DocAssemblyTemplateBody expectedRequestBody = baseExpectedRequestBodyBuilder()
            .partyName(DEFENDANT_NAME)
            .partyAddress(CCDAddress.builder().addressLine1("NEW ADDRESS1")
                .addressLine2("NEW ADDRESS2")
                .postCode("NEW POSTCODE").build())
            .build();

        assertEquals(expectedRequestBody, requestBody);
    }

    @Test
    void shouldMapNoticeOfTransferToCcbcLetterBodyForDefendant() {

        DocAssemblyTemplateBody requestBody = noticeOfTransferLetterTemplateMapper
            .noticeOfTransferToCcbcLetterBodyForDefendant(ccdCase, AUTHORISATION);

        DocAssemblyTemplateBody expectedRequestBody = baseExpectedRequestBodyBuilder()
            .partyName(DEFENDANT_NAME)
            .partyAddress(defendantAddress)
            .hearingCourtName(null)
            .hearingCourtAddress(null)
            .reasonForTransfer(null)
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

    private DocAssemblyTemplateBody.DocAssemblyTemplateBodyBuilder listingRequestBodyBuilder() {

        DocAssemblyTemplateBody.DocAssemblyTemplateBodyBuilder bodyBuilder = DocAssemblyTemplateBody
            .builder()
            .referenceNumber(CASE_REFERENCE)
            .currentDate(LocalDate.parse(TRANSFER_DATE))
            .hearingCourtName(TRANSFER_COURT_NAME)
            .hearingCourtAddress(transferCourtAddress)
            .reasonForTransfer(LISTING)
            .caseworkerName(CASE_WORKER_NAME);

        return bodyBuilder;
    }
}
