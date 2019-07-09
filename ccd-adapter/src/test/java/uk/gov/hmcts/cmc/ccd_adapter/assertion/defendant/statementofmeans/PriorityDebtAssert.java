package uk.gov.hmcts.cmc.ccd_adapter.assertion.defendant.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPriorityDebt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;

import java.util.Objects;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.ccd_adapter.assertion.Assertions.assertMoney;

public class PriorityDebtAssert extends AbstractAssert<PriorityDebtAssert, PriorityDebt> {

    public PriorityDebtAssert(PriorityDebt actual) {
        super(actual, PriorityDebtAssert.class);
    }

    public PriorityDebtAssert isEqualTo(CCDPriorityDebt ccdPriorityDebt) {
        isNotNull();

        if (!Objects.equals(actual.getType().name(), ccdPriorityDebt.getType().name())) {
            failWithMessage("Expected PriorityDebt.type to be <%s> but was <%s>",
                ccdPriorityDebt.getType(), actual.getType());
        }

        if (!Objects.equals(actual.getFrequency().name(), ccdPriorityDebt.getFrequency().name())) {
            failWithMessage("Expected PriorityDebt.frequency to be <%s> but was <%s>",
                ccdPriorityDebt.getFrequency().name(), actual.getFrequency().name());
        }

        assertMoney(actual.getAmount())
            .isEqualTo(
                ccdPriorityDebt.getAmount(),
                format("Expected PriorityDebt.amount to be <%s> but was <%s>",
                    ccdPriorityDebt.getAmount(), actual.getAmount()
                )
            );

        return this;
    }
}
