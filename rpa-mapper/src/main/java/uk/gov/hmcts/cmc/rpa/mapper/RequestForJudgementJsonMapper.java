package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

@Component
public class RequestForJudgementJsonMapper {

    public JsonObject map(Claim claim) {
        CountyCourtJudgment countyCourtJudgment = claim.getCountyCourtJudgment();
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("ccjRequestedOn", DateFormatter.format(claim.getCountyCourtJudgmentRequestedAt()))
            .add("amountWithInterest", claim.getTotalAmountTillToday().orElse(null))
            .add("courtFee", claim.getClaimData().getFeesPaidInPound())
            .add("alreadyPaid", countyCourtJudgment.getPaidAmount().orElse(null))
            .add("payment", getPaymentValue(countyCourtJudgment))
            .add("instalmentAmount", countyCourtJudgment.getRepaymentPlan()
                .map(RepaymentPlan::getInstalmentAmount).orElse(null))
            .add("firstPayment", countyCourtJudgment.getRepaymentPlan()
                .map(RepaymentPlan::getFirstPaymentDate).map(DateFormatter::format).orElse(null))
            .add("fullPaymentDeadline", countyCourtJudgment.getPayBySetDate().map(DateFormatter::format).orElse(null))
            .add("claimantEmailAddress", claim.getSubmitterEmail())
            .add("defendantEmailAddress", claim.getDefendantEmail())
            .build();
    }

    private String getPaymentValue(CountyCourtJudgment countyCourtJudgment) {
        PaymentOption paymentOption = countyCourtJudgment.getPaymentOption();
        switch (paymentOption) {
            case IMMEDIATELY:
                return paymentOption.getDescription();
            case INSTALMENTS:
                return countyCourtJudgment.getRepaymentPlan().map(repaymentPlan -> repaymentPlan.getPaymentSchedule()
                    .getDescription()).orElseThrow(IllegalStateException::new);
            case FULL_BY_SPECIFIED_DATE:
                return paymentOption.getDescription();
            default:
                throw new IllegalStateException("Payment option not found");
        }
    }
}

