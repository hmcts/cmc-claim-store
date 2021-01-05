package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestType;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.HWFCaseWorkerRespondSlaCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;
import uk.gov.hmcts.cmc.domain.utils.MonetaryConversions;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.valueOf;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.NO_INTEREST;

@Service
public class HWFFeeRemittedCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(CaseEvent.HWF_FULL_REMISSION_GRANTED);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CaseDetailsConverter caseDetailsConverter;

    private final DirectionsQuestionnaireDeadlineCalculator deadlineCalculator;

    private final HWFCaseWorkerRespondSlaCalculator hwfCaseWorkerRespondSlaCalculator;

    private final CaseMapper caseMapper;

    private final EventProducer eventProducer;

    private final UserService userService;

    private static final String INTEREST_NEEDS_RECALCULATED_ERROR_MESSAGE = "Help with Fees interest "
        + "needs to be recalculated. To proceed select 'Recalculate Interest/Claim Fee'";

    @Autowired
    public HWFFeeRemittedCallbackHandler(CaseDetailsConverter caseDetailsConverter,
                                         DirectionsQuestionnaireDeadlineCalculator deadlineCalculator,
                                         CaseMapper caseMapper,
                                         EventProducer eventProducer,
                                         UserService userService,
                                         HWFCaseWorkerRespondSlaCalculator hwfCaseWorkerRespondSlaCalculator) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.deadlineCalculator = deadlineCalculator;
        this.caseMapper = caseMapper;
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
        logger.info("HWF Remittance fee about-to-submit callback ");
        final var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();
        final CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        LocalDate hwfCaseWorkerSlaDate = hwfCaseWorkerRespondSlaCalculator.calculate(ccdCase.getSubmittedOn());
        if (!ccdCase.getInterestType().equals(CCDInterestType.NO_INTEREST)
            && LocalDateTime.now().toLocalDate().isAfter(hwfCaseWorkerSlaDate)
            && (ccdCase.getLastInterestCalculationDate() == null
            || (ccdCase.getLastInterestCalculationDate() != null
            && !LocalDateTime.now().toLocalDate()
            .isEqual(ccdCase.getLastInterestCalculationDate().toLocalDate())))) {
            String validationMessage = INTEREST_NEEDS_RECALCULATED_ERROR_MESSAGE;
            List<String> errors = new ArrayList<>();
            errors.add(validationMessage);
            responseBuilder.errors(errors);
            return responseBuilder.build();
        } else {
            BigDecimal feeAmountInPennies = NumberUtils.parseNumber(ccdCase.getFeeAmountInPennies(), BigDecimal.class);
            BigDecimal totalAmount = NumberUtils.parseNumber(ccdCase.getTotalAmount(), BigDecimal.class);
            totalAmount = totalAmount.subtract(feeAmountInPennies);
            ccdCase.setFeeAmountAfterRemission(valueOf(feeAmountInPennies));
            ccdCase.setTotalAmount(valueOf(totalAmount));
            responseBuilder.data(caseDetailsConverter.convertToMap(ccdCase));
        }
        return responseBuilder.build();
    }

    private CallbackResponse updateFeeRemitted1(CallbackParams callbackParams) {
        logger.info("HWF Remittance fee about-to-submit callback ");
        CallbackRequest callbackRequest = callbackParams.getRequest();

        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        // Check to see if 5 days have elapsed from Claim Submission days
        final Interest interest = claim.getClaimData().getInterest();
        LocalDate hwfCaseWorkerSlaDate = hwfCaseWorkerRespondSlaCalculator.calculate(claim.getCreatedAt());
        if (
            interest != null
                && interest.getType() != NO_INTEREST
                && LocalDateTime.now().toLocalDate().isAfter(hwfCaseWorkerSlaDate)
                && (interest.getLastInterestCalculationDate() == null
                || (interest.getLastInterestCalculationDate() != null
                && !LocalDateTime.now().toLocalDate()
                .isEqual(interest.getLastInterestCalculationDate().toLocalDate())))) {

            String validationMessage = INTEREST_NEEDS_RECALCULATED_ERROR_MESSAGE;
            var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();
            List<String> errors = new ArrayList<>();
            errors.add(validationMessage);
            responseBuilder.errors(errors);
            return responseBuilder.build();
        }
        if (!FeaturesUtils.isOnlineDQ(claim)) {
            LocalDate deadline = deadlineCalculator.calculate(LocalDateTime.now());
            claim = claim.toBuilder().directionsQuestionnaireDeadline(deadline).build();
        }
        Map<String, Object> dataMap = caseDetailsConverter.convertToMap(caseMapper.to(claim));
        Optional<BigDecimal> feesPaid = claim.getClaimData().getFeesPaidInPounds();

        if (feesPaid.isPresent()) {
            dataMap.put("feeRemitted", MonetaryConversions
                .poundsToPennies(feesPaid.get()).toString());
        }
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(dataMap)
            .build();
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
