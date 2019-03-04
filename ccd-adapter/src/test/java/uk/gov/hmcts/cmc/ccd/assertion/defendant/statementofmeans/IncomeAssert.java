package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;

import java.util.Objects;

public class IncomeAssert extends AbstractAssert<IncomeAssert, Income> {

    public IncomeAssert(Income actual) {
        super(actual, IncomeAssert.class);
    }

    public IncomeAssert isEqualTo(CCDIncome ccdIncome) {
        isNotNull();

        if (!Objects.equals(actual.getType().name(), ccdIncome.getType().name())) {
            failWithMessage("Expected Income.type to be <%s> but was <%s>",
                ccdIncome.getType(), actual.getType());
        }

        if (!Objects.equals(actual.getFrequency().name(), ccdIncome.getFrequency().name())) {
            failWithMessage("Expected Income.frequency to be <%s> but was <%s>",
                ccdIncome.getFrequency().name(), actual.getFrequency().name());
        }

        if (!Objects.equals(actual.getAmount(), ccdIncome.getAmountReceived())) {
            failWithMessage("Expected Income.amount to be <%s> but was <%s>",
                ccdIncome.getAmountReceived(), actual.getAmount());
        }

        if (!Objects.equals(actual.getOtherSource().orElse(null), ccdIncome.getOtherSource())) {
            failWithMessage("Expected Income.otherSource to be <%s> but was <%s>",
                ccdIncome.getOtherSource(), actual.getOtherSource());
        }

        return this;
    }
}
