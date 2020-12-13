package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;
import uk.gov.hmcts.cmc.domain.utils.MonetaryConversions;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class HWFPartRemissionCallbackHandler extends CallbackHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String PART_REMISSION_EQUAL_ERROR_MESSAGE =
        "Remitted fee is same as the total fee. For full remission, "
            + "please cancel and select the next step as \"Full remission HWF-granted\"";
    private static final String PART_REMISSION_IS_MORE_ERROR_MESSAGE = "Remitted fee should be less than the total fee";

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);

    private static final List<CaseEvent> EVENTS = ImmutableList.of(CaseEvent.HWF_PART_REMISSION_GRANTED);

    private final CaseDetailsConverter caseDetailsConverter;

    private final DirectionsQuestionnaireDeadlineCalculator deadlineCalculator;

    private final CaseMapper caseMapper;

    private final EventProducer eventProducer;

    private final UserService userService;

    private String validationMessage;

    @Autowired
    public HWFPartRemissionCallbackHandler(CaseDetailsConverter caseDetailsConverter,
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
            CallbackType.ABOUT_TO_SUBMIT, this::updateFeeRemitted,
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

    private CallbackResponse updateFeeRemitted(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();

        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        claim = validationResultForRemittedFee(claim);
        List<String> errors = new ArrayList<>();
        var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();
        if (null != validationMessage) {
            errors.add(validationMessage);
            responseBuilder.errors(errors);
        } else {
            if (!FeaturesUtils.isOnlineDQ(claim)) {
                LocalDate deadline = deadlineCalculator.calculate(LocalDateTime.now());
                claim = claim.toBuilder().directionsQuestionnaireDeadline(deadline).build();
            }

            Map<String, Object> dataMap = caseDetailsConverter.convertToMap(caseMapper.to(claim));

            responseBuilder.data(dataMap);
        }
        return responseBuilder.build();
    }

    private Claim validationResultForRemittedFee(Claim claim) {
        BigDecimal feedPaidInPounds;
        BigInteger feedPaidInPennies = null;
        BigDecimal feeAmountAfterRemissionInPounds;
        BigInteger feeAmountAfterRemissionInPennies = null;
        BigDecimal remittedFeesInPounds;
        BigInteger remittedFeesInPennies = null;
        Optional<BigDecimal> feesPaid = claim.getClaimData().getFeesPaidInPounds();
        Optional<BigDecimal> remittedFees = claim.getClaimData().getRemittedFeesInPounds();
        if (feesPaid.isPresent()) {
            feedPaidInPounds = feesPaid.get();
            feedPaidInPennies = MonetaryConversions.poundsToPennies(feedPaidInPounds);
        }
        if (remittedFees.isPresent()) {
            remittedFeesInPounds = remittedFees.get();
            remittedFeesInPennies = MonetaryConversions.poundsToPennies(remittedFeesInPounds);

            int value;
            if (null != feedPaidInPennies && null != remittedFeesInPennies) {
                value = feedPaidInPennies.compareTo(remittedFeesInPennies);
                if (value == 0) {
                    validationMessage = PART_REMISSION_EQUAL_ERROR_MESSAGE;
                } else if (value < 0) {
                    validationMessage = PART_REMISSION_IS_MORE_ERROR_MESSAGE;
                }
            }

            // Update feesAfterRemission
            feedPaidInPounds = feesPaid.get();
            feedPaidInPennies = MonetaryConversions.poundsToPennies(feedPaidInPounds);
            feeAmountAfterRemissionInPennies = feedPaidInPennies.subtract(remittedFeesInPennies);
            ClaimData claimData = claim.getClaimData().toBuilder()
                .feeAmountAfterRemission(feeAmountAfterRemissionInPennies).build();
            claim.toBuilder().claimData(claimData).build();
            return claim.toBuilder().claimData(claimData).build();
        }
        return claim;
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
