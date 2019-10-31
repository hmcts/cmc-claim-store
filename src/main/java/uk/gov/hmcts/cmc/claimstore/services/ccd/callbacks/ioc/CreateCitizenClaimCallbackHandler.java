package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CITIZEN_CLAIM;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Service
@Conditional(FeesAndPaymentsConfiguration.class)
public class CreateCitizenClaimCallbackHandler extends CallbackHandler {
    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_CITIZEN_CLAIM);
    private static final List<Role> ROLES = Collections.singletonList(CITIZEN);

    private final ImmutableMap<CallbackType, Callback> callbacks = ImmutableMap.of(
        CallbackType.ABOUT_TO_SUBMIT, this::createCitizenClaim,
        CallbackType.SUBMITTED, this::startClaimIssuedPostOperations
    );

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CaseDetailsConverter caseDetailsConverter;
    private final IssueDateCalculator issueDateCalculator;
    private final ReferenceNumberRepository referenceNumberRepository;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final CaseMapper caseMapper;
    private final PaymentsService paymentsService;
    private final EventProducer eventProducer;
    private final UserService userService;

    @Autowired
    public CreateCitizenClaimCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        IssueDateCalculator issueDateCalculator,
        ReferenceNumberRepository referenceNumberRepository,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        CaseMapper caseMapper,
        PaymentsService paymentsService,
        EventProducer eventProducer,
        UserService userService
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.issueDateCalculator = issueDateCalculator;
        this.referenceNumberRepository = referenceNumberRepository;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.caseMapper = caseMapper;
        this.paymentsService = paymentsService;
        this.eventProducer = eventProducer;
        this.userService = userService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return callbacks;
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
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());
        logger.info("Created citizen case for callback of type {}, claim with external id {}",
            callbackParams.getType(),
            claim.getExternalId());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        Payment payment = paymentsService.retrievePayment(authorisation, claim);

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            logger.info("Payment not successful for claim with external id {}", claim.getExternalId());

            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(ImmutableList.of("Payment not successful"))
                .build();
        }

        LocalDate issuedOn = issueDateCalculator.calculateIssueDay(nowInLocalZone());

        Claim updatedClaim = claim.toBuilder()
            .channel(ChannelType.CITIZEN)
            .claimData(claim.getClaimData().toBuilder()
                .payment(payment)
                .build())
            .referenceNumber(referenceNumberRepository.getReferenceNumberForCitizen())
            .issuedOn(issuedOn)
            .responseDeadline(responseDeadlineCalculator.calculateResponseDeadline(issuedOn))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(caseMapper.to(updatedClaim)))
            .build();
    }

    private CallbackResponse startClaimIssuedPostOperations(CallbackParams callbackParams) {
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());
        logger.info("Created citizen case for callback of type {}, claim with external id {}",
            callbackParams.getType(),
            claim.getExternalId());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        User user = userService.getUser(authorisation);
        eventProducer.createClaimCreatedEvent(
            claim,
            user.getUserDetails().getFullName(),
            authorisation
        );
        return SubmittedCallbackResponse.builder().build();
    }
}
