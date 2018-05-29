package uk.gov.hmcts.cmc.rpa.mapper;

import jdk.nashorn.internal.runtime.options.Option;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/*
*           case number
*           date of issue
amount claimed including interest
*           court fee
amount already paid
payment deadline
 */
@Component
public class RequestForJudgementJsonMapper {

    public JsonObject map(Claim claim) {
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("issueDate", DateFormatter.format(claim.getIssuedOn()))
            .add("courtFee", claim.getClaimData().getFeesPaidInPound())
            .add("alreadyPaid", formatMoney(claim.getCountyCourtJudgment().getPaidAmount().orElse(new BigDecimal(0))))
            .add("paymentDeadline", getPaymentDeadline(claim.getCountyCourtJudgment()))
            .build();
    }

    private String getPaymentDeadline(CountyCourtJudgment countyCourtJudgment) {
        String value;
        switch (countyCourtJudgment.getPaymentOption()) {
            case IMMEDIATELY:
                value = "Forthwith";
            break;
            case FULL_BY_SPECIFIED_DATE:
                value = "In Full by (" + DateFormatter.format(countyCourtJudgment.getPayBySetDate().get()) + ")";
            break;
            case INSTALMENTS:
                value = getInstallmentValues(countyCourtJudgment.getRepaymentPlan().get());
                break;
            default:
                throw new IllegalArgumentException("No payment option selected");

        }
        return value;
    }

    private String getInstallmentValues(RepaymentPlan repaymentPlan) {
        PaymentSchedule paymentSchedule = repaymentPlan.getPaymentSchedule();
        switch (paymentSchedule) {
            case EACH_WEEK:
                return "Weekly " + DateFormatter.format(repaymentPlan.getFirstPaymentDate());
            break;
            case EVERY_TWO_WEEKS:
                return "Fortnightly " + DateFormatter.format(repaymentPlan.getFirstPaymentDate());
            break;
            case EVERY_MONTH:
                return "Monthly " + DateFormatter.format(repaymentPlan.getFirstPaymentDate());
            break;
            default:
                new IllegalArgumentException("No Payment Schedule selected");
                break;
        }
    }

    private static String formatMoney(BigDecimal amount) {
        requireNonNull(amount);
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-GB")).format(amount);
    }

}

