package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;

import java.util.Objects;

public class DebtAssert extends AbstractAssert<DebtAssert, Debt> {

    public DebtAssert(Debt actual) {
        super(actual, DebtAssert.class);
    }

    public DebtAssert isEqualTo(CCDDebt ccdDebt) {
        isNotNull();

        if (!Objects.equals(actual.getDescription(), ccdDebt.getDescription())) {
            failWithMessage("Expected Debt.description to be <%s> but was <%s>",
                ccdDebt.getDescription(), actual.getDescription());
        }

        if (!Objects.equals(actual.getTotalOwed(), ccdDebt.getTotalOwed())) {
            failWithMessage("Expected Debt.totalOwed to be <%s> but was <%s>",
                ccdDebt.getTotalOwed(), actual.getTotalOwed());
        }

        if (!Objects.equals(actual.getMonthlyPayments(), ccdDebt.getMonthlyPayments())) {
            failWithMessage("Expected Debt.monthlyPayments to be <%s> but was <%s>",
                ccdDebt.getMonthlyPayments(), actual.getMonthlyPayments());
        }

        return this;
    }
}
