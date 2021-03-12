package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestEndDateType;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestType;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.HWFCaseWorkerRespondSlaCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class HWFFullAndPartRemissionCallbackHandler extends CallbackHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String PART_REMISSION_EQUAL_ERROR_MESSAGE =
        "Remitted fee is same as the total fee. For full remission, "
            + "please cancel and select the next step as \"Full remission HWF-granted\"";
    private static final String PART_REMISSION_IS_MORE_ERROR_MESSAGE = "Remitted fee should be less than the total fee";

    private static final String INTEREST_NEEDS_RECALCULATED_ERROR_MESSAGE = "Help with Fees interest "
        + "needs to be recalculated. To proceed select 'Recalculate Interest/Claim Fee'";

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);

    private static final List<CaseEvent> EVENTS = Arrays.asList(CaseEvent.HWF_PART_REMISSION_GRANTED,
        CaseEvent.HWF_FULL_REMISSION_GRANTED);

    private final CaseDetailsConverter caseDetailsConverter;

    private final HWFCaseWorkerRespondSlaCalculator hwfCaseWorkerRespondSlaCalculator;

    private final EventProducer eventProducer;

    private final UserService userService;

    private String validationMessage;

    @Autowired
    public HWFFullAndPartRemissionCallbackHandler(CaseDetailsConverter caseDetailsConverter,
                                                  EventProducer eventProducer,
                                                  UserService userService,
                                                  HWFCaseWorkerRespondSlaCalculator hwfCaseWorkerRespondSlaCalculator) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.eventProducer = eventProducer;
        this.userService = userService;
        this.hwfCaseWorkerRespondSlaCalculator = hwfCaseWorkerRespondSlaCalculator;
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
        final var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();
        final CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        validationMessage = null;

        LocalDate hwfCaseWorkerSlaDate = hwfCaseWorkerRespondSlaCalculator.calculate(ccdCase.getSubmittedOn());

        // Check to see if 5 days have elapsed from Claim Submission days
        if (!ccdCase.getInterestType().equals(CCDInterestType.NO_INTEREST)
            && LocalDateTime.now().toLocalDate().isAfter(hwfCaseWorkerSlaDate)
            && ccdCase.getInterestEndDateType().equals(CCDInterestEndDateType.SETTLED_OR_JUDGMENT)
            && (ccdCase.getLastInterestCalculationDate() == null
            || (ccdCase.getLastInterestCalculationDate() != null
            && !LocalDateTime.now().toLocalDate()
            .isEqual(ccdCase.getLastInterestCalculationDate().toLocalDate())))) {
            validationMessage = INTEREST_NEEDS_RECALCULATED_ERROR_MESSAGE;
        } else if (callbackParams.getRequest().getEventId().equals(CaseEvent.HWF_FULL_REMISSION_GRANTED.getValue())) {
            validationResultForRemittedFee(ccdCase, CaseEvent.HWF_FULL_REMISSION_GRANTED.getValue());
        } else if (callbackParams.getRequest().getEventId().equals(CaseEvent.HWF_PART_REMISSION_GRANTED.getValue())) {
            validationResultForRemittedFee(ccdCase, CaseEvent.HWF_PART_REMISSION_GRANTED.getValue());
        }
        List<String> errors = new ArrayList<>();
        if (null != validationMessage) {
            errors.add(validationMessage);
            responseBuilder.errors(errors);
        } else {
            ccdCase.setLastEventTriggeredForHwfCase(callbackParams.getRequest().getEventId());
            responseBuilder.data(caseDetailsConverter.convertToMap(ccdCase));
        }
        return responseBuilder.build();
    }

    private CCDCase validationResultForRemittedFee(CCDCase ccdCase, String eventType) {

        if (ccdCase.getFeeRemitted() != null && eventType.equals(CaseEvent.HWF_PART_REMISSION_GRANTED.getValue())) {
            BigDecimal feeAmountInPennies = NumberUtils.parseNumber(ccdCase.getFeeAmountInPennies(), BigDecimal.class);
            BigDecimal feeRemitted = NumberUtils.parseNumber(ccdCase.getFeeRemitted(), BigDecimal.class);
            BigDecimal totalAmount = NumberUtils.parseNumber(ccdCase.getTotalAmount(), BigDecimal.class);
            int value = feeAmountInPennies.compareTo(feeRemitted);
            if (value == 0) {
                validationMessage = PART_REMISSION_EQUAL_ERROR_MESSAGE;
            } else if (value < 0) {
                validationMessage = PART_REMISSION_IS_MORE_ERROR_MESSAGE;
            }
            totalAmount = totalAmount.subtract(feeRemitted);
            BigDecimal feeAmountAfterRemission = feeAmountInPennies.subtract(feeRemitted);
            ccdCase.setFeeAmountInPennies(valueOf(feeAmountAfterRemission));
            ccdCase.setTotalAmount(valueOf(totalAmount));
        } else if (eventType.equals(CaseEvent.HWF_FULL_REMISSION_GRANTED.getValue())) {
            BigDecimal feeAmountInPennies = NumberUtils.parseNumber(ccdCase.getFeeAmountInPennies(), BigDecimal.class);
            BigDecimal feeRemitted = feeAmountInPennies;
            BigDecimal totalAmount = NumberUtils.parseNumber(ccdCase.getTotalAmount(), BigDecimal.class);
            totalAmount = totalAmount.subtract(feeAmountInPennies);
            BigDecimal feeAmountAfterRemission = feeAmountInPennies.subtract(feeRemitted);
            ccdCase.setFeeAmountInPennies(valueOf(feeAmountAfterRemission));
            ccdCase.setFeeRemitted(valueOf(feeAmountInPennies));
            ccdCase.setTotalAmount(valueOf(totalAmount));
        }
        return ccdCase;
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
}
