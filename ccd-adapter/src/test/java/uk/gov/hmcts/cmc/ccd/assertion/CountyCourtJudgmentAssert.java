package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentSchedule;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class CountyCourtJudgmentAssert extends CustomAssert<CountyCourtJudgmentAssert, CountyCourtJudgment> {

    CountyCourtJudgmentAssert(CountyCourtJudgment actual) {
        super("CountyCourtJudgment", actual, CountyCourtJudgmentAssert.class);
    }

    public CountyCourtJudgmentAssert isEqualTo(CCDCountyCourtJudgment expected) {
        isNotNull();

        compare("defendantDateOfBirth",
            expected.getDefendantDateOfBirth(),
            actual.getDefendantDateOfBirth());

        compare("payBySetDate",
            expected.getPayBySetDate(),
            actual.getPayBySetDate());

        compare("paidAmount",
            expected.getPaidAmount(),
            actual.getPaidAmount(),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("paymentOption",
            expected.getPaymentOption(), Enum::name,
            Optional.ofNullable(actual.getPaymentOption()).map(Enum::name));

        Optional<RepaymentPlan> actualRepaymentPlan = actual.getRepaymentPlan();

        compare("repaymentPlanCompletionDate",
            expected.getRepaymentPlanCompletionDate(),
            actualRepaymentPlan.map(RepaymentPlan::getCompletionDate));

        compare("repaymentPlanFirstPaymentDate",
            expected.getRepaymentPlanFirstPaymentDate(),
            actualRepaymentPlan.map(RepaymentPlan::getFirstPaymentDate));

        compare("repaymentPlanInstalmentAmount",
            expected.getRepaymentPlanInstalmentAmount(),
            actualRepaymentPlan.map(RepaymentPlan::getInstalmentAmount),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("repaymentPlanPaymentLength",
            expected.getRepaymentPlanPaymentLength(),
            actualRepaymentPlan.map(RepaymentPlan::getPaymentLength));

        compare("repaymentPlanPaymentSchedule",
            expected.getRepaymentPlanPaymentSchedule(), CCDPaymentSchedule::getDescription,
            actualRepaymentPlan.map(RepaymentPlan::getPaymentSchedule).map(PaymentSchedule::getDescription));

        compare("statementOfTruthSignerName",
            expected.getStatementOfTruthSignerName(),
            actual.getStatementOfTruth().map(StatementOfTruth::getSignerName));

        compare("statementOfTruthSignerRole",
            expected.getStatementOfTruthSignerRole(),
            actual.getStatementOfTruth().map(StatementOfTruth::getSignerRole));

        return this;
    }
}
