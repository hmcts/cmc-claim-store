package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.helper.RPAMapperHelper;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

import static java.math.BigDecimal.ZERO;

@Component
public class RequestForJudgementJsonMapper {

    public JsonObject map(Claim claim) {
        CountyCourtJudgment countyCourtJudgment = claim.getCountyCourtJudgment();
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("ccjRequestedOn", DateFormatter.format(claim.getCountyCourtJudgmentRequestedAt()))
            .add("amountWithInterest", claim.getAmountWithInterest().orElse(null))
            .add("courtFee", claim.getClaimData().getFeesPaidInPounds().orElse(ZERO))
            .add("alreadyPaid", countyCourtJudgment.getPaidAmount().orElse(null))
            .add("paymentType", countyCourtJudgment.getPaymentOption().name())
            .add("fullPaymentDeadline", countyCourtJudgment.getPayBySetDate().map(DateFormatter::format).orElse(null))
            .add("instalments", countyCourtJudgment.getRepaymentPlan().map(RPAMapperHelper::toJson).orElse(null))
            .add("claimantEmailAddress", claim.getSubmitterEmail())
            .add("defendantEmailAddress", claim.getDefendantEmail())
            .build();
    }
}

