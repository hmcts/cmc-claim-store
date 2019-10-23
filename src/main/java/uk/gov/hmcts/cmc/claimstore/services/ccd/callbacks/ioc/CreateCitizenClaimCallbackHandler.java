package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.ChannelType;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_LEGAL_REP_CLAIM;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Service
public class CreateCitizenClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_LEGAL_REP_CLAIM);
    private static final List<Role> ROLES = Collections.singletonList(CITIZEN);

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CaseDetailsConverter caseDetailsConverter;
    private final IssueDateCalculator issueDateCalculator;
    private final ReferenceNumberRepository referenceNumberRepository;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final CaseMapper caseMapper;
    private final PaymentsService paymentsService;

    @Autowired
    public CreateCitizenClaimCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        IssueDateCalculator issueDateCalculator,
        ReferenceNumberRepository referenceNumberRepository,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        CaseMapper caseMapper,
        PaymentsService paymentsService
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.issueDateCalculator = issueDateCalculator;
        this.referenceNumberRepository = referenceNumberRepository;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.caseMapper = caseMapper;
        this.paymentsService = paymentsService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(CallbackType.ABOUT_TO_SUBMIT, this::createCitizenClaim);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private CallbackResponse createCitizenClaim(CallbackParams callbackParams) {
        logger.info("Created citizen case for callback of type {}", callbackParams.getType());
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());

        LocalDate issuedOn = issueDateCalculator.calculateIssueDay(nowInLocalZone());
        LocalDate responseDeadline = responseDeadlineCalculator.calculateResponseDeadline(issuedOn);
        String referenceNumber = referenceNumberRepository.getReferenceNumberForLegal();
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        Payment payment = paymentsService.retrievePayment(authorisation, claim);
        Claim updatedClaim = null;

        if (payment.getStatus().equals(PaymentStatus.SUCCESS)) {
            updatedClaim = claim.toBuilder()
                .referenceNumber(referenceNumber)
                .issuedOn(issuedOn)
                .responseDeadline(responseDeadline)
                .claimData(claim.getClaimData().toBuilder()
                    .payment(payment)
                    .build())
                .channel(ChannelType.CITIZEN)
                .build();
        } else {
            updatedClaim = claim.toBuilder()
                .claimData(claim.getClaimData().toBuilder()
                    .payment(payment)
                    .build())
                .channel(ChannelType.CITIZEN)
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(caseMapper.to(updatedClaim)))
            .build();
    }
}
