package uk.gov.hmcts.cmc.ccd.adapter.assertion.defendant.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;

import java.util.Objects;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.ccd.adapter.assertion.Assertions.assertMoney;

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

        assertMoney(actual.getTotalOwed())
            .isEqualTo(
                ccdDebt.getTotalOwed(),
                format("Expected Debt.totalOwed to be <%s> but was <%s>",
                    ccdDebt.getTotalOwed(), actual.getTotalOwed()
                )
            );

        assertMoney(actual.getMonthlyPayments())
            .isEqualTo(
                ccdDebt.getMonthlyPayments(),
                format("Expected Debt.monthlyPayments to be <%s> but was <%s>",
                    ccdDebt.getMonthlyPayments(), actual.getMonthlyPayments()
                )
            );

        return this;
    }
}
