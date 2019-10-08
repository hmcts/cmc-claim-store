package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;

import static java.math.BigDecimal.TEN;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CASE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.getLegalDataWithReps;

@RunWith(MockitoJUnitRunner.class)
public class CreateCitizenClaimCallbackHandlerTest {

    public static final String REFERENCE_NO = "000LR001";
    public static final LocalDate ISSUE_DATE = now();
    public static final LocalDate RESPONSE_DEADLINE= ISSUE_DATE.plusDays(14);
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

    private CallbackParams callbackParams;
    private CallbackRequest callbackRequest;
    private CreateCitizenClaimCallbackHandler createCitizenClaimCallbackHandler;

    private CaseDetails caseDetails = CaseDetails.builder().id(3L).data(Collections.emptyMap()).build();

    @Before
    public void setUp() {
        createCitizenClaimCallbackHandler =
            new CreateCitizenClaimCallbackHandler(
            caseDetailsConverter,
            issueDateCalculator,
            referenceNumberRepository,
            responseDeadlineCalculator,
            caseMapper,
            paymentsService
        );

        callbackRequest = CallbackRequest
            .builder()
            .eventId(CREATE_CASE.getValue())
            .caseDetails(caseDetails)
            .build();

        Mockito.when(issueDateCalculator.calculateIssueDay(any())).thenReturn(ISSUE_DATE);
        Mockito.when(responseDeadlineCalculator.calculateResponseDeadline(ISSUE_DATE)).thenReturn(RESPONSE_DEADLINE);
        Mockito.when(referenceNumberRepository.getReferenceNumberForLegal()).thenReturn(REFERENCE_NO);
        Mockito.when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(getLegalDataWithReps());
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponse() {

        OffsetDateTime paymentDate = OffsetDateTime.parse("2017-02-03T10:15:30+01:00");

        Payment expectedPayment = Payment.builder()
            .amount(TEN)
            .reference("reference")
            .status(SUCCESS)
            .dateCreated(paymentDate.toLocalDate().toString())
            .nextUrl(NEXT_URL)
            .build();

        Mockito.when(paymentsService.retrievePayment(eq(BEARER_TOKEN), any(Claim.class))).thenReturn(expectedPayment);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            createCitizenClaimCallbackHandler.handle(callbackParams);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getWarnings()).isNull();
    }

    @Test
    public void shouldHaveCorrectLegalRepSupportingRole() {
        assertThat(createCitizenClaimCallbackHandler.getSupportedRoles().size()).isEqualTo(1);
        assertThat(createCitizenClaimCallbackHandler.getSupportedRoles()).contains(CITIZEN);
    }
}
