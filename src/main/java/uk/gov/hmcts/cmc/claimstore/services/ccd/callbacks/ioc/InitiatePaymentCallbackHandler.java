package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INITIATE_CLAIM_PAYMENT_CITIZEN;
import static uk.gov.hmcts.cmc.domain.models.ChannelType.CITIZEN;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;
import static uk.gov.hmcts.cmc.domain.utils.MonetaryConversions.poundsToPennies;

@Service
@Conditional(FeesAndPaymentsConfiguration.class)
public class InitiatePaymentCallbackHandler extends CallbackHandler {
    private static final List<CaseEvent> EVENTS = Collections.singletonList(INITIATE_CLAIM_PAYMENT_CITIZEN);
    private static final List<Role> ROLES = Collections.singletonList(Role.CITIZEN);

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PaymentsService paymentsService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseMapper caseMapper;
    private final IssueDateCalculator issueDateCalculator;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;

    @Autowired
    public InitiatePaymentCallbackHandler(
        PaymentsService paymentsService,
        CaseDetailsConverter caseDetailsConverter,
        CaseMapper caseMapper,
        IssueDateCalculator issueDateCalculator,
        ResponseDeadlineCalculator responseDeadlineCalculator
    ) {
        this.paymentsService = paymentsService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseMapper = caseMapper;
        this.issueDateCalculator = issueDateCalculator;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_SUBMIT, this::createPayment
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private CallbackResponse createPayment(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        String authorisation = callbackParams.getParams()
            .get(CallbackParams.Params.BEARER_TOKEN).toString();

        Claim claim = caseDetailsConverter.extractClaim(caseDetails);

        logger.info("Initiating payment for callback of type {}, claim with external id {}",
            callbackParams.getType(),
            claim.getExternalId());

        LocalDate issuedOn = issueDateCalculator.calculateIssueDay(nowInLocalZone());
        LocalDate responseDeadline = responseDeadlineCalculator.calculateResponseDeadline(issuedOn);

        Claim updatedClaim = claim.toBuilder()
            .ccdCaseId(caseDetails.getId())
            .issuedOn(issuedOn)
            .responseDeadline(responseDeadline)
            .channel(CITIZEN)
            .build();

        logger.info("Creating payment for claim with external id {}",
            updatedClaim.getExternalId());

        Payment payment = paymentsService.createPayment(
            authorisation,
            updatedClaim
        );

        Claim claimAfterPayment = updatedClaim.toBuilder()
            .claimData(updatedClaim.getClaimData().toBuilder()
                .payment(payment)
                .feeAmountInPennies(poundsToPennies(payment.getAmount()))
                .build())
            .build();

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(caseMapper.to(claimAfterPayment)))
            .build();
    }
}
