package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class HWFMiscellaneousCallbackHandler extends CallbackHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);

    private static final List<CaseEvent> EVENTS = Arrays.asList(CaseEvent.MISC_HWF,
        CaseEvent.MORE_INFO_REQUIRED_FOR_HWF, CaseEvent.FULL_REMISSION_HWF_REJECTED,
        CaseEvent.PART_REMISSION_HWF_REJECTED, CaseEvent.HWF_NO_REMISSION);

    private final CaseDetailsConverter caseDetailsConverter;

    private final DirectionsQuestionnaireDeadlineCalculator deadlineCalculator;

    private final CaseMapper caseMapper;

    private final EventProducer eventProducer;

    private final UserService userService;

    private static final String INTEREST_NEEDS_RECALCULATED_ERROR_MESSAGE = "Help with Fees interest "
        + "needs to be recalculated. To proceed select 'Recalculate Interest/Claim Fee'";

    @Autowired
    public HWFMiscellaneousCallbackHandler(CaseDetailsConverter caseDetailsConverter,
                                           DirectionsQuestionnaireDeadlineCalculator deadlineCalculator,
                                           CaseMapper caseMapper, EventProducer eventProducer,
                                           UserService userService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.deadlineCalculator = deadlineCalculator;
        this.caseMapper = caseMapper;
        this.eventProducer = eventProducer;
        this.userService = userService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_SUBMIT, this::hwfupdateInfo,
            CallbackType.SUBMITTED, this::startHwfClaimUpdatePostOperations
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

    private CallbackResponse hwfupdateInfo(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();

        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();
        if (callbackRequest.getEventId().equals(CaseEvent.HWF_NO_REMISSION)
            && LocalDateTime.now().isAfter(claim.getCreatedAt().plusDays(5))) {
            String validationMessage = INTEREST_NEEDS_RECALCULATED_ERROR_MESSAGE;
            List<String> errors = new ArrayList<>();
            errors.add(validationMessage);
            responseBuilder.errors(errors);
        }
        if (!FeaturesUtils.isOnlineDQ(claim)) {
            LocalDate deadline = deadlineCalculator.calculate(LocalDateTime.now());
            claim = claim.toBuilder().directionsQuestionnaireDeadline(deadline).build();
        }

        Map<String, Object> dataMap = caseDetailsConverter.convertToMap(caseMapper.to(claim));

        responseBuilder.data(dataMap);

        return responseBuilder.build();
    }

    private CallbackResponse startHwfClaimUpdatePostOperations(CallbackParams callbackParams) {
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());
        logger.info("Created citizen case for callback of type {}, claim with external id {}",
            callbackParams.getType(),
            claim.getExternalId());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        User user = userService.getUser(authorisation);
        eventProducer.createHwfClaimUpdatedEvent(
            claim,
            user.getUserDetails().getFullName(),
            authorisation
        );
        return SubmittedCallbackResponse.builder().build();
    }
}
