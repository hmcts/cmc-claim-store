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
import uk.gov.hmcts.cmc.domain.utils.MonetaryConversions;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RESUME_CLAIM_PAYMENT_CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Service
@Conditional(FeesAndPaymentsConfiguration.class)
public class ResumePaymentCallbackHandler extends CallbackHandler {
    private static final List<CaseEvent> EVENTS = Collections.singletonList(RESUME_CLAIM_PAYMENT_CITIZEN);
    private static final List<Role> ROLES = Collections.singletonList(CITIZEN);

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PaymentsService paymentsService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseMapper caseMapper;
    private final IssueDateCalculator issueDateCalculator;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;

    @Autowired
    public ResumePaymentCallbackHandler(
        PaymentsService paymentsService,
        CaseDetailsConverter caseDetailsConverter,
        CaseMapper caseMapper,
        IssueDateCalculator issueDateCalculator,
        ResponseDeadlineCalculator responseDeadlineCalculator) {
        this.paymentsService = paymentsService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseMapper = caseMapper;
        this.issueDateCalculator = issueDateCalculator;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_SUBMIT, this::resumePayment
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

    private CallbackResponse resumePayment(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        String authorisation = callbackParams.getParams()
            .get(CallbackParams.Params.BEARER_TOKEN).toString();

        Claim claim = caseDetailsConverter.extractClaim(caseDetails);

        logger.info("Resuming payment for callback of type {}, claim with external id {}",
            callbackParams.getType(),
            claim.getExternalId());

        Claim claimAfterPayment = withPayment(authorisation, claim);

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(caseMapper.to(claimAfterPayment)))
            .build();
    }

    private Claim withPayment(String authorisation, Claim claim) {
        Payment originalPayment = paymentsService.retrievePayment(authorisation, claim);

        logger.info("Retrieved payment from pay hub with status {}, claim with external id {}",
            originalPayment.getStatus().toString(),
            claim.getExternalId());

        if (originalPayment.getStatus().equals(SUCCESS)) {
            return claim.toBuilder()
                .claimData(claim.getClaimData().toBuilder()
                    .payment(originalPayment)
                    .build())
                .build();
        }

        LocalDate issuedOn = issueDateCalculator.calculateIssueDay(nowInLocalZone());
        LocalDate responseDeadline = responseDeadlineCalculator.calculateResponseDeadline(issuedOn);

        Claim updatedClaim = claim.toBuilder()
            .issuedOn(issuedOn)
            .serviceDate(issuedOn.plusDays(5))
            .responseDeadline(responseDeadline)
            .build();

        logger.info("Creating payment for claim with external id {}",
            updatedClaim.getExternalId());

        Payment newPayment = paymentsService.createPayment(
            authorisation,
            updatedClaim
        );

        return updatedClaim.toBuilder()
            .claimData(updatedClaim.getClaimData().toBuilder()
                .payment(newPayment)
                .feeAmountInPennies(MonetaryConversions.poundsToPennies(newPayment.getAmount()))
                .build())
            .build();

    }
}
