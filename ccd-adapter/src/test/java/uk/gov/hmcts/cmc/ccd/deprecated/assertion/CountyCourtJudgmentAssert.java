package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import org.junit.Assert;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;

import java.util.Objects;

public class CountyCourtJudgmentAssert extends AbstractAssert<CountyCourtJudgmentAssert, CountyCourtJudgment> {

    public CountyCourtJudgmentAssert(CountyCourtJudgment actual) {
        super(actual, CountyCourtJudgmentAssert.class);
    }

    public CountyCourtJudgmentAssert isEqualTo(CCDCountyCourtJudgment ccdCountyCourtJudgment) {
        isNotNull();

        actual.getDefendantDateOfBirth().ifPresent(dob -> {
            if (!Objects.equals(dob, ccdCountyCourtJudgment.getDefendantDateOfBirth())) {
                failWithMessage(
                    "Expected CountyCourtJudgment.defendantDateOfBirth to be <%s> but was <%s>",
                    ccdCountyCourtJudgment.getDefendantDateOfBirth(), actual.getDefendantDateOfBirth());
            }
        });

        actual.getPayBySetDate().ifPresent(payBysetDate -> {
            if (!Objects.equals(payBysetDate, ccdCountyCourtJudgment.getPayBySetDate())) {
                failWithMessage("Expected CountyCourtJudgment.payBySetDate to be <%s> but was <%s>",
                    ccdCountyCourtJudgment.getPayBySetDate(), actual.getPayBySetDate());
            }
        });

        if (!Objects.equals(actual.getPaidAmount().orElse(null),
            ccdCountyCourtJudgment.getPaidAmount())) {
            failWithMessage("Expected CountyCourtJudgment.paidAmount to be <%s> but was <%s>",
                ccdCountyCourtJudgment.getPaidAmount(), actual.getPaidAmount());
        }

        if (!Objects.equals(actual.getPaymentOption().name(), ccdCountyCourtJudgment.getPaymentOption().name())) {
            failWithMessage("Expected CountyCourtJudgment.paymentOption to be <%s> but was <%s>",
                ccdCountyCourtJudgment.getPaymentOption().name(), actual.getPaymentOption().name());
        }

        actual.getRepaymentPlan()
            .ifPresent(repaymentPlan -> {
                Assert.assertEquals(repaymentPlan.getCompletionDate(),
                    ccdCountyCourtJudgment.getRepaymentPlanCompletionDate());
                Assert.assertEquals(repaymentPlan.getFirstPaymentDate(),
                    ccdCountyCourtJudgment.getRepaymentPlanFirstPaymentDate());
                Assert.assertEquals(repaymentPlan.getInstalmentAmount(),
                    ccdCountyCourtJudgment.getRepaymentPlanInstalmentAmount());
                Assert.assertEquals(repaymentPlan.getPaymentLength(),
                    ccdCountyCourtJudgment.getRepaymentPlanPaymentLength());
                Assert.assertEquals(repaymentPlan.getPaymentSchedule().getDescription(),
                    ccdCountyCourtJudgment.getRepaymentPlanPaymentSchedule().getDescription());
            });

        actual.getStatementOfTruth().ifPresent(statementOfTruth -> {
            Assert.assertEquals(statementOfTruth.getSignerName(),
                ccdCountyCourtJudgment.getStatementOfTruthSignerName());
            Assert.assertEquals(statementOfTruth.getSignerRole(),
                ccdCountyCourtJudgment.getStatementOfTruthSignerRole());
        });

        return this;
    }
}
