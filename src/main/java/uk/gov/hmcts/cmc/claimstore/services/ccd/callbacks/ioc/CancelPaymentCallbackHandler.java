package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.services.PaymentsService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CANCEL_CLAIM_PAYMENT_CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.INITIATED;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.PENDING;

@Service
@Conditional(FeesAndPaymentsConfiguration.class)
public class CancelPaymentCallbackHandler extends CallbackHandler {
    private static final List<CaseEvent> EVENTS = Collections.singletonList(CANCEL_CLAIM_PAYMENT_CITIZEN);
    private static final List<Role> ROLES = Collections.singletonList(CITIZEN);
    private static final Set<PaymentStatus> CANCELLABLE_STATUSES = ImmutableSet.of(PENDING, INITIATED);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PaymentsService paymentsService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseMapper caseMapper;

    @Autowired
    public CancelPaymentCallbackHandler(
        PaymentsService paymentsService,
        CaseDetailsConverter caseDetailsConverter,
        CaseMapper caseMapper
    ) {
        this.paymentsService = paymentsService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseMapper = caseMapper;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_SUBMIT, this::cancelPayment
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

    private CallbackResponse cancelPayment(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        String authorisation = callbackParams.getParams()
            .get(CallbackParams.Params.BEARER_TOKEN).toString();

        Claim claim = caseDetailsConverter.extractClaim(caseDetails);

        logger.info("Cancelling payment for callback of type {}, claim with external id {}",
            callbackParams.getType(),
            claim.getExternalId());

        Claim claimAfterCancelledPayment = withCancelledPayment(authorisation, claim);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(caseMapper.to(claimAfterCancelledPayment)))
            .build();
    }

    private Claim withCancelledPayment(String authorisation, Claim claim) {
        Payment originalPayment = paymentsService.retrievePayment(authorisation, claim.getClaimData()).orElseThrow(
            () -> new NotFoundException("No payment found in claim with external id " + claim.getExternalId())
        );

        logger.info("Retrieved payment from pay hub with status {}, claim with external id {}",
            originalPayment.getStatus(),
            claim.getExternalId());

        if (!CANCELLABLE_STATUSES.contains(originalPayment.getStatus())) {
            return claim.toBuilder()
                .claimData(claim.getClaimData().toBuilder()
                    .payment(originalPayment)
                    .build())
                .build();
        }

        logger.info("Cancelling payment for claim with external id {}", claim.getExternalId());

        paymentsService.cancelPayment(authorisation, originalPayment.getReference());

        Payment cancelledPayment = paymentsService.retrievePayment(authorisation, claim.getClaimData())
            .orElseThrow(() -> new IllegalStateException(format(
                "Expected cancelled payment with reference %s but got null",
                originalPayment.getReference()))
            );

        return claim.toBuilder()
            .claimData(claim.getClaimData().toBuilder()
                .payment(cancelledPayment)
                .build())
            .build();

    }
}
