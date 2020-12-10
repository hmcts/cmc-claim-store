package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;
import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RECALCULATE_INTEREST;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator.calculateInterest;
import static uk.gov.hmcts.cmc.domain.utils.MonetaryConversions.poundsToPennies;

@Service
public class HWFRecalculateInterestCallbackHandler extends CallbackHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String FEE_EVENT = "issue";
    private static final String FEE_CHANNEL = "online";

    private static final String INTEREST_NOT_CLAIMED = "Recalculation is not required as interest is not claimed";
    private static final String NOT_HWF_CLAIM = "Recalculation is not required for non HWF Claims";
    private static final String INTEREST_NOT_CLAIMED_TILL_JUDGEMENT = "Recalculation is not required as interest is "
        + "not claimed till final judgement";

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(RECALCULATE_INTEREST);

    private final FeesClient feesClient;
    private final CaseDetailsConverter caseDetailsConverter;

    @Autowired
    public HWFRecalculateInterestCallbackHandler(CaseDetailsConverter caseDetailsConverter, FeesClient feesClient) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.feesClient = feesClient;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(ABOUT_TO_SUBMIT, this::recalculateInterest);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private CallbackResponse recalculateInterest(CallbackParams callbackParams) {
        final var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();
        final CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        final CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        final Claim claim = caseDetailsConverter.extractClaim(caseDetails);

        final String errorMessage = validateClaim(claim);
        if (errorMessage != null) {
            responseBuilder.errors(asList(errorMessage));
        } else {
            recalculateInterest(claim, ccdCase);
            responseBuilder.data(caseDetailsConverter.convertToMap(ccdCase));
        }

        return responseBuilder.build();
    }

    private void recalculateInterest(Claim claim, CCDCase ccdCase) {
        final BigDecimal calculatedInterest = calculateInterest(claim, now()).get();
        ccdCase.setLastInterestCalculationDate(LocalDateTime.now());
        ccdCase.setCurrentInterestAmount(valueOf(poundsToPennies(calculatedInterest)));

        final BigDecimal claimAmount = ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount();
        final BigDecimal claimAmountPlusInterest = claimAmount.add(calculatedInterest);

        final FeeLookupResponseDto feeOutcome = feesClient.lookupFee(FEE_CHANNEL, FEE_EVENT, claimAmountPlusInterest);
        ccdCase.setFeeAmountInPennies(valueOf(poundsToPennies(feeOutcome.getFeeAmount())));

        final BigDecimal totalAmountIncludingFee = claimAmountPlusInterest.add(feeOutcome.getFeeAmount());
        ccdCase.setTotalAmount(valueOf(poundsToPennies(totalAmountIncludingFee)));
    }

    private String validateClaim(Claim claim) {
        final ClaimData claimData = claim.getClaimData();
        final Interest interest = claimData.getInterest();
        if (!claimData.getHelpWithFeesNumber().isPresent()) {
            return NOT_HWF_CLAIM;
        } else if (interest == null) {
            return INTEREST_NOT_CLAIMED;
        } else if (interest.getInterestDate() == null || !interest.getInterestDate().isEndDateOnClaimComplete()) {
            return INTEREST_NOT_CLAIMED_TILL_JUDGEMENT;
        }
        return null;
    }
}
