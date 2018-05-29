package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;
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
            .add("alreadyPaid", formatMoney(claim.getCountyCourtJudgment().getPaidAmount().orElse(null)))
            .add("paymentDeadline", getPaymentDeadline(claim.getCountyCourtJudgment()))
            .build();
    }

    private String getPaymentDeadline(CountyCourtJudgment countyCourtJudgment) {
        switch (countyCourtJudgment.getPaymentOption()) {
            case IMMEDIATELY:
                return "Forthwith";
            case FULL_BY_SPECIFIED_DATE:
                return "In Full by (" + DateFormatter.format(countyCourtJudgment.getPayBySetDate().get()) + ")";
            case INSTALMENTS:
                return getRepaymentSchedule(countyCourtJudgment.getRepaymentPlan().get());
            default:
                throw new IllegalArgumentException("No payment option selected");

        }
    }

    private String getRepaymentSchedule(RepaymentPlan repaymentPlan) {
        PaymentSchedule paymentSchedule = repaymentPlan.getPaymentSchedule();
        String firstPaymentDate = DateFormatter.format(repaymentPlan.getFirstPaymentDate());
        switch (paymentSchedule) {
            case EACH_WEEK:
                return "Weekly " + firstPaymentDate;
            case EVERY_TWO_WEEKS:
                return "Fortnightly " + firstPaymentDate;
            case EVERY_MONTH:
                return "Monthly " + firstPaymentDate;
            default:
                throw new IllegalArgumentException("No Payment Schedule selected");
        }
    }

    private static String formatMoney(BigDecimal amount) {
        requireNonNull(amount);
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-GB")).format(amount);
    }

}

