package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static java.lang.String.format;
import static java.math.BigDecimal.TEN;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_HWF_CASE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.FAILED;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.withFullClaimData;

@RunWith(MockitoJUnitRunner.class)
public class CreateHelpWithFeesClaimCallbackHandlerTest {
    private static final String REFERENCE_NO = "000MC001";
    private static final LocalDate ISSUE_DATE = now();
    private static final LocalDate RESPONSE_DEADLINE = ISSUE_DATE.plusDays(14);
    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String NEXT_URL = "http://nexturl.test";

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

    @Mock
    private EventProducer eventProducer;

    @Mock
    private UserService userService;

    @Captor
    private ArgumentCaptor<Claim> claimArgumentCaptor;

    private CallbackParams callbackParams;
    private CallbackRequest callbackRequest;
    private CreateHelpWithFeesClaimCallbackHandler createHelpWithFeesClaimCallbackHandler;

    private final CaseDetails caseDetails = CaseDetails.builder().id(3L).data(Collections.emptyMap()).build();

    @Before
    public void setUp() {

        createHelpWithFeesClaimCallbackHandler = new CreateHelpWithFeesClaimCallbackHandler(
            caseDetailsConverter,
            issueDateCalculator,
            referenceNumberRepository,
            responseDeadlineCalculator,
            caseMapper,
            eventProducer,
            userService
        );

        callbackRequest = CallbackRequest.builder()
            .eventId(CREATE_HWF_CASE.getValue())
            .caseDetails(caseDetails)
            .build();

        when(issueDateCalculator.calculateIssueDay(any())).thenReturn(ISSUE_DATE);
        when(responseDeadlineCalculator.calculateResponseDeadline(ISSUE_DATE)).thenReturn(RESPONSE_DEADLINE);
        when(referenceNumberRepository.getReferenceNumberForCitizen()).thenReturn(REFERENCE_NO);
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponse() {

        Claim claim = SampleClaim.getDefault().toBuilder()
            .referenceNumber(referenceNumberRepository.getReferenceNumberForCitizen())
            .issuedOn(ISSUE_DATE)
            .responseDeadline(RESPONSE_DEADLINE)
            .claimData(withFullClaimData().getClaimData())
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        createHelpWithFeesClaimCallbackHandler.handle(callbackParams);

        verify(caseMapper).to(claimArgumentCaptor.capture());

        Claim toBeSaved = claimArgumentCaptor.getValue();
        assertThat(toBeSaved.getIssuedOn()).contains(ISSUE_DATE);
        assertThat(toBeSaved.getServiceDate()).isEqualTo(ISSUE_DATE.plusDays(5));
        assertThat(toBeSaved.getReferenceNumber()).isEqualTo(REFERENCE_NO);
        assertThat(toBeSaved.getResponseDeadline()).isEqualTo(RESPONSE_DEADLINE);
    }

}
