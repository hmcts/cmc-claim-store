package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class HWFMiscellaneousCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = Arrays.asList(CaseEvent.UPDATE_HWF_NUMBER,
        CaseEvent.MORE_INFO_REQUIRED_FOR_HWF,
        CaseEvent.HWF_NO_REMISSION);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CaseDetailsConverter caseDetailsConverter;

    private final EventProducer eventProducer;

    private final UserService userService;

    @Autowired
    public HWFMiscellaneousCallbackHandler(CaseDetailsConverter caseDetailsConverter,
                                           EventProducer eventProducer,
                                           UserService userService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.eventProducer = eventProducer;
        this.userService = userService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_START, this::hwfMoreInfoMidEventCallback,
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
        final var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();
        final CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        if (callbackParams.getRequest().getEventId().equals(CaseEvent.HWF_NO_REMISSION.getValue())) {
            ccdCase.setFeeRemitted("0");
        }
        ccdCase.setLastEventTriggeredForHwfCase(callbackParams.getRequest().getEventId());
        responseBuilder.data(caseDetailsConverter.convertToMap(ccdCase));
        return responseBuilder.build();
    }

    private CallbackResponse startHwfClaimUpdatePostOperations(CallbackParams callbackParams) {

        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails())
            .toBuilder().lastEventTriggeredForHwfCase(callbackParams.getRequest().getEventId()).build();
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

    private CallbackResponse hwfMoreInfoMidEventCallback(CallbackParams callbackParams) {

        final var responseBuilder
            = AboutToStartOrSubmitCallbackResponse.builder();
        if (callbackParams.getRequest().getEventId().equals(CaseEvent.MORE_INFO_REQUIRED_FOR_HWF.getValue())) {
            final CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
            CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
            ccdCase.setHwfDocumentsToBeSentBefore(LocalDate.now().plusDays(14));
            responseBuilder.data(caseDetailsConverter.convertToMap(ccdCase));
        }
        return responseBuilder.build();
    }
}
