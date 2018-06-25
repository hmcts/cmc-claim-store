package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import java.util.Optional;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

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
            .add("instalments", getInstalments(countyCourtJudgment.getRepaymentPlan()))
            .add("claimantEmailAddress", claim.getSubmitterEmail())
            .add("defendantEmailAddress", claim.getDefendantEmail())
            .build();
    }

    private JsonObject getInstalments(Optional<RepaymentPlan> repaymentPlan) {
        JsonObjectBuilder jsonObjectBuilder = new NullAwareJsonObjectBuilder()
            .add("amount", repaymentPlan.map(RepaymentPlan::getInstalmentAmount).orElse(null))
            .add("firstPayment", repaymentPlan
                .map(RepaymentPlan::getFirstPaymentDate).map(DateFormatter::format).orElse(null))
            .add("frequency", repaymentPlan.map(value -> value.getPaymentSchedule().name())
                .orElse(null));

        return jsonObjectBuilder.build();
    }
}

