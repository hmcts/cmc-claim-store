package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INITIATE_CLAIM_PAYMENT_CITIZEN;

@Service
public class InitiatePaymentCallbackHandler extends CallbackHandler {

    public static final String PAYMENT_NEXT_URL = "paymentNextUrl";
    private static final String CASE_ID = "id";

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_SUBMIT, this::createPayment
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(INITIATE_CLAIM_PAYMENT_CITIZEN);
    }

    private CallbackResponse createPayment(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(ImmutableMap.<String, Object>builder()
                .putAll(caseDetails.getData())
                .put(CASE_ID, caseDetails.getId())
                .put(PAYMENT_NEXT_URL, "http://payment_next_url.test")
                .build())
            .build();
    }
}
