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
import uk.gov.hmcts.cmc.domain.models.InterestDate;
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
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.now;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RECALCULATE_INTEREST;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator.calculateBreakdownInterest;
import static uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator.calculateFixedRateInterest;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.BREAKDOWN;
import static uk.gov.hmcts.cmc.domain.utils.MonetaryConversions.poundsToPennies;

@Service
public class HWFRecalculateInterestCallbackHandler extends CallbackHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String FEE_EVENT = "issue";
    private static final String FEE_CHANNEL = "online";

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

        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);

        if (recalculateInterest(claim)) {
            BigDecimal calculatedInterest = calculateInterest(claim);
            ccdCase.setLastInterestCalculationDate(LocalDateTime.now());
            ccdCase.setCurrentInterestAmount(valueOf(poundsToPennies(calculatedInterest)));

            BigDecimal claimAmount = ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount();
            BigDecimal claimAmountPlusInterest = claimAmount.add(calculatedInterest);

            FeeLookupResponseDto feeOutcome = feesClient.lookupFee(FEE_CHANNEL, FEE_EVENT, claimAmountPlusInterest);
            ccdCase.setFeeAmountInPennies(valueOf(poundsToPennies(feeOutcome.getFeeAmount())));

            BigDecimal totalAmountIncludingFee = claimAmountPlusInterest.add(feeOutcome.getFeeAmount());
            ccdCase.setTotalAmount(valueOf(poundsToPennies(totalAmountIncludingFee)));
        }

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(ccdCase))
            .build();
    }

    private BigDecimal calculateInterest(Claim claim) {
        ClaimData claimData = claim.getClaimData();
        Interest interest = claimData.getInterest();
        BigDecimal claimAmount = ((AmountBreakDown) claimData.getAmount()).getTotalAmount();

        if (interest.getType() == BREAKDOWN) {
            return calculateBreakdownInterest(interest, interest.getInterestDate(), claimAmount,
                claim.getIssuedOn().get(), now());
        } else {
            return calculateFixedRateInterest(claim, now()).orElse(ZERO);
        }
    }

    private boolean recalculateInterest(Claim claim) {
        ClaimData claimData = claim.getClaimData();
        Interest interest = claimData.getInterest();
        boolean isHelpWithFeeClaim = claimData.getHelpWithFeesNumber().isPresent();
        InterestDate interestDate = interest != null ? interest.getInterestDate() : null;
        return isHelpWithFeeClaim && interestDate != null && interestDate.isEndDateOnClaimComplete();
    }
}
