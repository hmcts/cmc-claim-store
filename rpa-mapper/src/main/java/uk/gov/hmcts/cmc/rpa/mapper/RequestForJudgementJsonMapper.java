package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
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
            .add("paymentType", countyCourtJudgment.getPaymentOption().name())
            .add("fullPaymentDeadline", countyCourtJudgment.getPayBySetDate().map(DateFormatter::format).orElse(null))
            .add("instalments", countyCourtJudgment.getRepaymentPlan().map(this::toJson).orElse(toNullValuedJson()))
            .add("claimantEmailAddress", claim.getSubmitterEmail())
            .add("defendantEmailAddress", claim.getDefendantEmail())
            .build();
    }

    private JsonObject toJson(RepaymentPlan repaymentPlan) {
        return new NullAwareJsonObjectBuilder()
            .add("amount", repaymentPlan.getInstalmentAmount())
            .add("firstPayment", DateFormatter.format(repaymentPlan.getFirstPaymentDate()))
            .add("frequency", repaymentPlan.getPaymentSchedule().name())
            .build();

    }

    private JsonObject toNullValuedJson() {
        return new NullAwareJsonObjectBuilder()
            .addNull("amount")
            .addNull("firstPayment")
            .addNull("frequency")
            .build();
    }
}

