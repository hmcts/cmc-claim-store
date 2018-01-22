package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;

import java.util.Objects;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class CountyCourtJudgmentAssert extends AbstractAssert<CountyCourtJudgmentAssert, CountyCourtJudgment> {

    public CountyCourtJudgmentAssert(CountyCourtJudgment actual) {
        super(actual, CountyCourtJudgmentAssert.class);
    }

    public CountyCourtJudgmentAssert isEqualTo(CCDCountyCourtJudgment ccdCountyCourtJudgment) {
        isNotNull();

        if (!Objects.equals(actual.getDefendantDateOfBirth().orElse(null).format(ISO_DATE_TIME),
            ccdCountyCourtJudgment.getDefendantDateOfBirth())) {
            failWithMessage("Expected CountyCourtJudgment.defendantDateOfBirth to be <%s> but was <%s>",
                ccdCountyCourtJudgment.getDefendantDateOfBirth(), actual.getDefendantDateOfBirth());
        }

        if (!Objects.equals(actual.getPaidAmount().orElse(null),
            ccdCountyCourtJudgment.getPaidAmount())) {
            failWithMessage("Expected CountyCourtJudgment.paidAmount to be <%s> but was <%s>",
                ccdCountyCourtJudgment.getPaidAmount(), actual.getPaidAmount());
        }

        if (!Objects.equals(actual.getPayBySetDate().orElse(null).format(ISO_DATE),
            ccdCountyCourtJudgment.getPayBySetDate())) {
            failWithMessage("Expected CountyCourtJudgment.payBySetDate to be <%s> but was <%s>",
                ccdCountyCourtJudgment.getPayBySetDate(), actual.getPayBySetDate());
        }

        if (!Objects.equals(actual.getPaymentOption().name(), ccdCountyCourtJudgment.getPaymentOption().name())) {
            failWithMessage("Expected CountyCourtJudgment.paymentOption to be <%s> but was <%s>",
                ccdCountyCourtJudgment.getPaymentOption().name(), actual.getPaymentOption().name());
        }

        actual.getRepaymentPlan()
            .ifPresent(repaymentPlan -> assertThat(repaymentPlan).isEqualTo(ccdCountyCourtJudgment.getRepaymentPlan()));

        actual.getStatementOfTruth().ifPresent(statementOfTruth ->
            assertThat(statementOfTruth).isEqualTo(ccdCountyCourtJudgment.getStatementOfTruth()));

        return this;
    }
}
