package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Collections;

import static java.math.BigDecimal.TEN;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CITIZEN_CLAIM;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.FAILED;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.withFullClaimData;

@RunWith(MockitoJUnitRunner.class)
public class CreateCitizenClaimCallbackHandlerTest {

    public static final String REFERENCE_NO = "000MC001";
    public static final LocalDate ISSUE_DATE = now();
    public static final LocalDate RESPONSE_DEADLINE = ISSUE_DATE.plusDays(14);
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
    private PaymentsService paymentsService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private UserService userService;

    @Captor
    private ArgumentCaptor<Claim> claimArgumentCaptor;

    private CallbackParams callbackParams;
    private CallbackRequest callbackRequest;
    private CreateCitizenClaimCallbackHandler createCitizenClaimCallbackHandler;

    private CaseDetails caseDetails = CaseDetails.builder().id(3L).data(Collections.emptyMap()).build();

    private Payment.PaymentBuilder paymentBuilder;

    @Before
    public void setUp() {
        paymentBuilder = Payment.builder()
            .amount(TEN)
            .reference("reference2")
            .dateCreated("2017-12-03")
            .nextUrl(NEXT_URL);

        createCitizenClaimCallbackHandler =
            new CreateCitizenClaimCallbackHandler(
                caseDetailsConverter,
                issueDateCalculator,
                referenceNumberRepository,
                responseDeadlineCalculator,
                caseMapper,
                paymentsService,
                eventProducer,userService
        );

        callbackRequest = CallbackRequest
            .builder()
            .eventId(CREATE_CITIZEN_CLAIM.getValue())
            .caseDetails(caseDetails)
            .build();

        Mockito.when(issueDateCalculator.calculateIssueDay(any())).thenReturn(ISSUE_DATE);
        Mockito.when(responseDeadlineCalculator.calculateResponseDeadline(ISSUE_DATE)).thenReturn(RESPONSE_DEADLINE);
        Mockito.when(referenceNumberRepository.getReferenceNumberForCitizen()).thenReturn(REFERENCE_NO);
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponseWhenSuccessfulPayment() {

        Mockito.when(paymentsService.retrievePayment(eq(BEARER_TOKEN), any(Claim.class)))
            .thenReturn(paymentBuilder.status(SUCCESS).build());

        Claim claim = SampleClaim.getDefault().toBuilder()
            .referenceNumber(referenceNumberRepository.getReferenceNumberForCitizen())
            .issuedOn(ISSUE_DATE)
            .responseDeadline(responseDeadlineCalculator.calculateResponseDeadline(now()))
            .claimData(withFullClaimData().getClaimData())
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            createCitizenClaimCallbackHandler.handle(callbackParams);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getWarnings()).isNull();

        verify(caseMapper).to(claimArgumentCaptor.capture());

        Claim toBeSaved = claimArgumentCaptor.getValue();
        assertThat(toBeSaved.getIssuedOn()).isEqualTo(ISSUE_DATE);
        assertThat(toBeSaved.getReferenceNumber()).isEqualTo(REFERENCE_NO);
        assertThat(toBeSaved.getResponseDeadline()).isEqualTo(RESPONSE_DEADLINE);
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponseWhenUnSuccessfulPayment() {

        Mockito.when(paymentsService.retrievePayment(eq(BEARER_TOKEN), any(Claim.class)))
            .thenReturn(paymentBuilder.status(FAILED).build());

        Claim claim = SampleClaim.withFullClaimDataAndFailedPayment();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            createCitizenClaimCallbackHandler.handle(callbackParams);

        assertThat(response.getWarnings()).containsOnly("Payment not successful");
    }

    @Test
    public void shouldHaveCorrectLegalRepSupportingRole() {
        assertThat(createCitizenClaimCallbackHandler.getSupportedRoles()).containsOnly(CITIZEN);
    }
}
