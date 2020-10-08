package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaperResponseReviewedHandlerTest {

    @InjectMocks
    private PaperResponseReviewedHandler paperResponseReviewedHandler;

    @Mock
    CaseDetailsConverter caseDetailsConverter;
    @Mock
    CaseMapper caseMapper;
    @Mock
    ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    MoreTimeRequestRule moreTimeRequestRule;
    @Mock
    NotificationService notificationService;
    @Mock
    NotificationsProperties notificationsProperties;

    @Mock
    private CallbackRequest callbackRequest;

    @Mock
    CallbackParams callbackParams;

    private static final String AUTHORISATION = "Bearer: abcd";

    @Before
    public void setup() {

        CCDCase ccdCase = SampleData.getCCDCitizenCase();

        Claim claim = SampleClaim.getCitizenClaim();

        CaseDetails caseDetailsBefore = CaseDetails.builder().id(1L).build();
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();

        callbackRequest = CallbackRequest.builder().caseDetailsBefore(caseDetailsBefore).caseDetails(caseDetails).build();

//        callbackParams = CallbackParams.builder()
//            .request(callbackRequest)
//            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
//            .build();

        when(callbackParams.getRequest()).thenReturn(callbackRequest);


//        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);

//        Map<String, Object> mappedCaseData = mock(Map.class);
//        lenient().doReturn(mappedCaseData).when(caseDetailsConverter).convertToMap(any(CCDCase.class));
//
//        ccdCase = ccdCase.toBuilder().transferContent(CCDTransferContent.builder().build()).build();
        when(caseDetailsConverter.extractClaim(caseDetails)).thenReturn(claim);

        paperResponseReviewedHandler = new PaperResponseReviewedHandler(caseDetailsConverter, caseMapper, responseDeadlineCalculator, moreTimeRequestRule,
            notificationService, notificationsProperties, callbackParams);
        System.out.println(paperResponseReviewedHandler);

    }

    @Test
    public void test(){
        paperResponseReviewedHandler.handle();

    }
}
