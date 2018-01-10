package uk.gov.hmcts.cmc.claimstore.services.interest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ClaimInterestService {

    private InterestCalculationService interestCalculationService;

    @Autowired
    public ClaimInterestService(InterestCalculationService interestCalculationService) {
        this.interestCalculationService = interestCalculationService;
    }

    public Claim calculateAndPopulateTotalAmount(Claim claim) {
        ClaimData data = claim.getClaimData();

        if (data.getAmount() instanceof AmountBreakDown) {
            BigDecimal claimAmount = ((AmountBreakDown) data.getAmount()).getTotalAmount();

            if (data.getInterest().getType() != Interest.InterestType.NO_INTEREST) {
                return claimWithInterest(claim, data, claimAmount);
            } else {
                return new Claim(
                    claim,
                    claimAmount.add(data.getFeesPaidInPound()),
                    claimAmount.add(data.getFeesPaidInPound())
                );
            }
        }

        return claim;
    }

    private Claim claimWithInterest(Claim claim, ClaimData data, BigDecimal claimAmount) {
        BigDecimal rate = data.getInterest().getRate();
        LocalDate fromDate = (data.getInterestDate().getType() == InterestDate.InterestDateType.SUBMISSION)
            ? claim.getCreatedAt().toLocalDate()
            : data.getInterestDate().getDate();

        BigDecimal interestTillToday = interestCalculationService.calculateInterestUpToNow(
            claimAmount, rate, fromDate
        );
        BigDecimal interestTillDateOfIssue = interestCalculationService.calculateInterest(
            claimAmount, rate, fromDate, claim.getCreatedAt().toLocalDate()
        );

        return new Claim(
            claim,
            interestTillToday.add(claimAmount).add(data.getFeesPaidInPound()),
            interestTillDateOfIssue.add(claimAmount).add(data.getFeesPaidInPound())
        );
    }
}
