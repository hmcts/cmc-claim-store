package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legalrep;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Collections;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.UPDATE_LEGAL_REP_CLAIM;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.SOLICITOR;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.getLegalDataWithReps;

@RunWith(MockitoJUnitRunner.class)
public class UpdateLegalRepClaimCallbackHandlerTest {

    public static final String REFERENCE_NO = "000LR001";
    public static final LocalDate ISSUE_DATE = now();
    public static final LocalDate RESPONSE_DEADLINE = ISSUE_DATE.plusDays(14);
    private static final String BEARER_TOKEN = "Bearer let me in";

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private IssueDateCalculator issueDateCalculator;
    @Mock
    private ReferenceNumberRepository referenceNumberRepository;
    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    private CaseMapper caseMapper;

    private CallbackRequest callbackRequest;
    private UpdateLegalRepClaimCallbackHandler updateLegalRepClaimCallbackHandler;

    private final CaseDetails caseDetails = CaseDetails.builder().id(3L).data(Collections.emptyMap()).build();

    @Before
    public void setUp() {
        updateLegalRepClaimCallbackHandler = new UpdateLegalRepClaimCallbackHandler(
            caseDetailsConverter,
            issueDateCalculator,
            referenceNumberRepository,
            responseDeadlineCalculator,
            caseMapper
        );

        callbackRequest = CallbackRequest
            .builder()
            .eventId(UPDATE_LEGAL_REP_CLAIM.getValue())
            .caseDetails(caseDetails)
            .build();
        CCDCase ccdCase = SampleData.getCCDLegalCase();
        ccdCase.setPaymentStatus("Success");
        when(issueDateCalculator.calculateIssueDay(any())).thenReturn(ISSUE_DATE);
        when(responseDeadlineCalculator.calculateResponseDeadline(ISSUE_DATE)).thenReturn(RESPONSE_DEADLINE);
        when(referenceNumberRepository.getReferenceNumberForLegal()).thenReturn(REFERENCE_NO);
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(getLegalDataWithReps());

    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponse() {
        CCDCase ccdCase = SampleData.getCCDLegalCase();
        ccdCase.setPaymentStatus("Success");
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            updateLegalRepClaimCallbackHandler.handle(callbackParams);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getWarnings()).isNull();
    }

    @Test
    public void shouldNotStoreResponseDeadlineWhenPaymentStatusIsNotSuccess() {
        CCDCase ccdCase = SampleData.getCCDLegalCase();
        ccdCase.setPaymentStatus("Failed");
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            updateLegalRepClaimCallbackHandler.handle(callbackParams);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getWarnings()).isNull();
    }

    @Test
    public void shouldHaveCorrectLegalRepSupportingRole() {
        assertThat(updateLegalRepClaimCallbackHandler.getSupportedRoles().size()).isEqualTo(1);
        assertThat(updateLegalRepClaimCallbackHandler.getSupportedRoles()).contains(SOLICITOR);
    }
}
