package uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDSelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;

import java.util.Objects;

public class SelfEmploymentAssert extends AbstractAssert<SelfEmploymentAssert, SelfEmployment> {

    public SelfEmploymentAssert(SelfEmployment actual) {
        super(actual, SelfEmploymentAssert.class);
    }

    public SelfEmploymentAssert isEqualTo(CCDSelfEmployment ccdSelfEmployment) {
        isNotNull();

        if (!Objects.equals(actual.getJobTitle(), ccdSelfEmployment.getJobTitle())) {
            failWithMessage("Expected SelfEmployment.jobTitle to be <%s> but was <%s>",
                ccdSelfEmployment.getJobTitle(), actual.getJobTitle());
        }

        if (!Objects.equals(actual.getAnnualTurnover(), ccdSelfEmployment.getAnnualTurnover())) {
            failWithMessage("Expected SelfEmployment.annualTurnover to be <%s> but was <%s>",
                ccdSelfEmployment.getAnnualTurnover(), actual.getAnnualTurnover());
        }

        actual.getOnTaxPayments().ifPresent(onTaxPayments -> {
            if (!Objects.equals(onTaxPayments.getAmountYouOwe(),
                ccdSelfEmployment.getOnTaxPayments().getAmountYouOwe())
                ) {
                failWithMessage("Expected OnTaxPayments.annualTurnover to be <%s> but was <%s>",
                    ccdSelfEmployment.getAnnualTurnover(), actual.getAnnualTurnover());
            }

            if (!Objects.equals(onTaxPayments.getReason(),
                ccdSelfEmployment.getOnTaxPayments().getReason())
                ) {
                failWithMessage("Expected OnTaxPayments.reason to be <%s> but was <%s>",
                    ccdSelfEmployment.getAnnualTurnover(), actual.getAnnualTurnover());
            }
        });

        return this;
    }
}
