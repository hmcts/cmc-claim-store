package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;
import uk.gov.hmcts.cmc.domain.utils.MonetaryConversions;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class HWFPartRemissionCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(CaseEvent.HWF_PART_REMISSION_GRANTED);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CaseDetailsConverter caseDetailsConverter;

    private final DirectionsQuestionnaireDeadlineCalculator deadlineCalculator;

    private final CaseMapper caseMapper;

    @Autowired
    public HWFPartRemissionCallbackHandler(CaseDetailsConverter caseDetailsConverter,
                                           DirectionsQuestionnaireDeadlineCalculator deadlineCalculator,
                                           CaseMapper caseMapper) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.deadlineCalculator = deadlineCalculator;
        this.caseMapper = caseMapper;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_SUBMIT, this::updateFeeRemitted
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
        logger.info("HWFPartRemissionCallbackHandler : updateFeeRemitted");
        CallbackRequest callbackRequest = callbackParams.getRequest();

        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());

        if (!FeaturesUtils.isOnlineDQ(claim)) {
            LocalDate deadline = deadlineCalculator.calculate(LocalDateTime.now());
            claim = claim.toBuilder().directionsQuestionnaireDeadline(deadline).build();
        }

        Map<String, Object> dataMap = caseDetailsConverter.convertToMap(caseMapper.to(claim));

        var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();

        String validationResult = validationResultForRemittedFee(claim);
        List<String> errors = new ArrayList<>();
        if (null != validationResult) {
            errors.add(validationResult);
            responseBuilder.errors(errors);
        } else {
            responseBuilder.data(dataMap);
        }
        return responseBuilder.build();
    }

    private String  validationResultForRemittedFee(Claim claim) {
        String validationMessage = null;
        BigDecimal feedPaidInPounds;
        BigInteger feedPaidInPennies = null;
        BigDecimal remittedFeesInPounds;
        BigInteger remittedFeesInPennies = null;
        if (claim.getClaimData().getFeesPaidInPounds().isPresent()) {
            feedPaidInPounds = claim.getClaimData().getFeesPaidInPounds().get();
            feedPaidInPennies = MonetaryConversions.poundsToPennies(feedPaidInPounds);
        }
        if (claim.getClaimData().getRemittedFeesInPounds().isPresent()) {
            remittedFeesInPounds = claim.getClaimData().getRemittedFeesInPounds().get();
            remittedFeesInPennies = MonetaryConversions.poundsToPennies(remittedFeesInPounds);
        }
        int value;
        if (null != feedPaidInPennies && null != remittedFeesInPennies) {
            value = feedPaidInPennies.compareTo(remittedFeesInPennies);
            if (value == 0) {
                validationMessage = "Remitted fee is same as the total fee. "
                    + "For full remission, please cancel and select the next step as \"Full remission HWF-granted\"";
            } else if (value < 0) {
                validationMessage = "Remitted fee should be less than the total fee";
            }
        }
        return validationMessage;
    }
}
