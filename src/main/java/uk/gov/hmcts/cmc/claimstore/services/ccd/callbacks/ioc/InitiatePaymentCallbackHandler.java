package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.mapper.InitiatePaymentCaseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.MoneyMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator;
import uk.gov.hmcts.cmc.domain.models.ioc.InitiatePaymentRequest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.payments.client.models.Payment;

import java.math.BigDecimal;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INITIATE_CLAIM_PAYMENT_CITIZEN;

@Service
public class InitiatePaymentCallbackHandler extends CallbackHandler {
    private static final String PAYMENT_AMOUNT = "paymentAmount";
    private static final String PAYMENT_REFERENCE = "paymentReference";
    private static final String PAYMENT_STATUS = "paymentStatus";
    private static final String PAYMENT_DATE_CREATED = "paymentDateCreated";
    public static final String PAYMENT_NEXT_URL = "paymentNextUrl";

    private static final String CASE_ID = "id";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PaymentsService paymentsService;
    private final InitiatePaymentCaseMapper initiatePaymentCaseMapper;
    private final CaseDetailsConverter caseDetailsConverter;
    private final MoneyMapper moneyMapper;

    @Autowired
    public InitiatePaymentCallbackHandler(
        PaymentsService paymentsService,
        InitiatePaymentCaseMapper initiatePaymentCaseMapper,
        CaseDetailsConverter caseDetailsConverter,
        MoneyMapper moneyMapper
    ) {
        this.paymentsService = paymentsService;
        this.initiatePaymentCaseMapper = initiatePaymentCaseMapper;
        this.caseDetailsConverter = caseDetailsConverter;
        this.moneyMapper = moneyMapper;
    }

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
        String authorisation = callbackParams.getParams()
            .get(CallbackParams.Params.BEARER_TOKEN).toString();

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        InitiatePaymentRequest request = initiatePaymentCaseMapper.from(ccdCase);

        logger.info("Calculating interest amount for case {}",
            ccdCase.getExternalId());

        BigDecimal totalAmount =
            TotalAmountCalculator.amountWithInterestUntilIssueDate(
            request.getAmount(),
            request.getInterest(),
            request.getIssuedOn()
        ).orElseThrow(IllegalStateException::new);

        logger.info("Creating payment in pay hub for case {}",
            ccdCase.getExternalId());

        Payment payment = paymentsService.makePayment(
            authorisation,
            ccdCase,
            totalAmount
        );

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(ImmutableMap.<String, Object>builder()
                .putAll(caseDetails.getData())
                .put(CASE_ID, caseDetails.getId())
                .put(PAYMENT_AMOUNT, moneyMapper.to(payment.getAmount()))
                .put(PAYMENT_REFERENCE, payment.getReference())
                .put(PAYMENT_STATUS, payment.getStatus())
                .put(PAYMENT_DATE_CREATED, payment.getDateCreated())
                .put(PAYMENT_NEXT_URL, payment.getLinks().getNextUrl().getHref().toString())
                .build())
            .build();
    }
}
