package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.UPDATE_CLAIM_PAYMENT;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePaymentCallbackHandlerTest {
    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String NEXT_URL = "http://nexturl.test";
    private static final Long CASE_ID = 3L;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseMapper caseMapper;
    @Captor
    private ArgumentCaptor<Claim> claimArgumentCaptor;

    private CallbackRequest callbackRequest;

    private UpdatePaymentCallbackHandler handler;

    @Before
    public void setUp() {
        handler = new UpdatePaymentCallbackHandler(
            caseDetailsConverter,
            caseMapper
        );
        callbackRequest = CallbackRequest
            .builder()
            .eventId(UPDATE_CLAIM_PAYMENT.getValue())
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .build())
            .build();
    }

    @Test
    public void shouldUpdatePaymentOnAboutToSubmitEvent() {
        Claim claim = SampleClaim.getDefault();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        handler.handle(callbackParams);

        verify(caseMapper).to(claimArgumentCaptor.capture());

        Claim toBeSaved = claimArgumentCaptor.getValue();
        assertThat(toBeSaved.getId()).isEqualTo(CASE_ID);

        Payment payment = toBeSaved.getClaimData().getPayment().orElse(null);
        Assert.assertNotNull(payment);
    }

    @Test
    public void shouldAcceptCitizenAndCaseWorkerRoles() {
        assertThat(handler.getSupportedRoles()).contains(Role.CITIZEN, Role.CASEWORKER);
    }
}
