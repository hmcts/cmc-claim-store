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
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CITIZEN_CLAIM;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_HWF_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INVALID_HWF_REFERENCE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_HWF_CASE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.AWAITING_RESPONSE_HWF;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.HWF_APPLICATION_PENDING;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Service
@Conditional(FeesAndPaymentsConfiguration.class)
public class CreateCitizenClaimCallbackHandler extends CallbackHandler {
    private static final List<CaseEvent> EVENTS = Arrays.asList(CREATE_CITIZEN_CLAIM,
        CREATE_HWF_CASE, INVALID_HWF_REFERENCE, ISSUE_HWF_CASE);
    private static final List<Role> ROLES = Arrays.asList(CITIZEN, CASEWORKER);

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
        Claim updatedClaim = null;
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());
        logger.info("Created citizen case for callback of type {}, claim with external id {}",
            callbackParams.getType(),
            claim.getExternalId());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        String eventTriggered = callbackParams.getRequest().getEventId();
        if (claim.getState().equals(HWF_APPLICATION_PENDING) || claim.getState().equals(AWAITING_RESPONSE_HWF)) {

            if (eventTriggered.equals(CREATE_HWF_CASE.getValue())
                || eventTriggered.equals(INVALID_HWF_REFERENCE.getValue())) {
                updatedClaim = claim.toBuilder()
                    .channel(ChannelType.CITIZEN)
                    .claimData(claim.getClaimData().toBuilder()
                        .build())
                    .referenceNumber(String.valueOf(claim.getId()))
                    .lastEventTriggeredForHwfCase(eventTriggered)
                    .build();
            }
        } else {
            if (eventTriggered.equals(ISSUE_HWF_CASE.getValue())) {
                LocalDate issuedOn = issueDateCalculator.calculateIssueDay(nowInLocalZone());

                updatedClaim = claim.toBuilder()
                    .channel(ChannelType.CITIZEN)
                    .claimData(claim.getClaimData().toBuilder()
                        .build())
                    .referenceNumber(referenceNumberRepository.getReferenceNumberForCitizen())
                    .createdAt(LocalDateTimeFactory.nowInUTC())
                    .issuedOn(issuedOn)
                    .serviceDate(issuedOn.plusDays(5))
                    .responseDeadline(responseDeadlineCalculator.calculateResponseDeadline(issuedOn))
                    .build();
            } else {
                Payment payment = paymentsService.retrievePayment(authorisation, claim.getClaimData())
                    .orElseThrow(() -> new IllegalStateException(format(
                        "Claim with external id %s has no payment record",
                        claim.getExternalId()))
                    );

                if (payment.getStatus() != PaymentStatus.SUCCESS) {
                    logger.info("Payment not successful for claim with external id {}", claim.getExternalId());

                    return AboutToStartOrSubmitCallbackResponse.builder()
                        .errors(ImmutableList.of("Payment not successful"))
                        .build();
                }

                LocalDate issuedOn = issueDateCalculator.calculateIssueDay(nowInLocalZone());

                updatedClaim = claim.toBuilder()
                    .channel(ChannelType.CITIZEN)
                    .claimData(claim.getClaimData().toBuilder()
                        .payment(payment)
                        .build())
                    .referenceNumber(referenceNumberRepository.getReferenceNumberForCitizen())
                    .createdAt(LocalDateTimeFactory.nowInUTC())
                    .issuedOn(issuedOn)
                    .serviceDate(issuedOn.plusDays(5))
                    .responseDeadline(responseDeadlineCalculator.calculateResponseDeadline(issuedOn))
                    .build();
            }
        }
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
        if (claim.getState().equals(AWAITING_RESPONSE_HWF)) {
            eventProducer.createHwfClaimUpdatedEvent(
                claim,
                user.getUserDetails().getFullName(),
                authorisation
            );
        } else {
            eventProducer.createClaimCreatedEvent(
                claim,
                user.getUserDetails().getFullName(),
                authorisation
            );
        }
        return SubmittedCallbackResponse.builder().build();
    }
}
