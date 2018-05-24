package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

@Component
public class RequestForJudgementJsonMapper {

    public JsonObject map(Claim claim) {
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("issueDate", DateFormatter.format(claim.getIssuedOn()))
            .add("courtFee", claim.getClaimData().getFeesPaidInPound())
            .add("alreadyPaid", formatMoney(claim.getCountyCourtJudgment().getPaidAmount().orElse(BigDecimal.ZERO)))
            //.add("paymentDeadline", claim.getCountyCourtJudgment().getRepaymentPlan().ifPresent(plan -> claim.getCountyCourtJudgment().getClass().repaymentPlan(repaymentPlanMapper.to(plan)))
            .build();
    }

    private static String formatMoney(BigDecimal amount) {
        requireNonNull(amount);
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-GB")).format(amount);
    }

}
