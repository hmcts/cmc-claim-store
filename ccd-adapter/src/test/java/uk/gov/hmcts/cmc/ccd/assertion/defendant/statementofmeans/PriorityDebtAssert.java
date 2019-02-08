package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPriorityDebt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;

import java.util.Objects;

public class PriorityDebtAssert extends AbstractAssert<PriorityDebtAssert, PriorityDebt> {

    public PriorityDebtAssert(PriorityDebt actual) {
        super(actual, PriorityDebtAssert.class);
    }

    public PriorityDebtAssert isEqualTo(CCDPriorityDebt ccdPriorityDebt) {
        isNotNull();

        if (!Objects.equals(actual.getType(), ccdPriorityDebt.getType())) {
            failWithMessage("Expected PriorityDebt.type to be <%s> but was <%s>",
                ccdPriorityDebt.getType(), actual.getType());
        }

        if (!Objects.equals(actual.getFrequency().name(), ccdPriorityDebt.getFrequency().name())) {
            failWithMessage("Expected PriorityDebt.frequency to be <%s> but was <%s>",
                ccdPriorityDebt.getFrequency().name(), actual.getFrequency().name());
        }

        if (!Objects.equals(actual.getAmount(), ccdPriorityDebt.getAmount())) {
            failWithMessage("Expected PriorityDebt.amount to be <%s> but was <%s>",
                ccdPriorityDebt.getAmount(), actual.getAmount());
        }

        return this;
    }
}
