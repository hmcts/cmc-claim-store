package uk.gov.hmcts.cmc.ccd.assertion.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDSelfEmployed;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployed;

import java.util.Objects;

public class SelfEmployedAssert extends AbstractAssert<SelfEmployedAssert, SelfEmployed> {

    public SelfEmployedAssert(SelfEmployed actual) {
        super(actual, SelfEmployedAssert.class);
    }

    public SelfEmployedAssert isEqualTo(CCDSelfEmployed ccdSelfEmployed) {
        isNotNull();

        if (!Objects.equals(actual.getJobTitle(), ccdSelfEmployed.getJobTitle())) {
            failWithMessage("Expected SelfEmployed.jobTitle to be <%s> but was <%s>",
                ccdSelfEmployed.getJobTitle(), actual.getJobTitle());
        }

        if (!Objects.equals(actual.getAnnualTurnover(), ccdSelfEmployed.getAnnualTurnover())) {
            failWithMessage("Expected SelfEmployed.annualTurnover to be <%s> but was <%s>",
                ccdSelfEmployed.getAnnualTurnover(), actual.getAnnualTurnover());
        }

        if (!Objects.equals(actual.isBehindOnTaxPayments().name(), ccdSelfEmployed.getBehindOnTaxPayments().name())) {
            failWithMessage("Expected SelfEmployed.behindTaxPayments to be <%s> but was <%s>",
                ccdSelfEmployed.getBehindOnTaxPayments().name(), actual.isBehindOnTaxPayments().name());
        }

        if (!Objects.equals(actual.getReason(), ccdSelfEmployed.getReason())) {
            failWithMessage("Expected SelfEmployed.reason to be <%s> but was <%s>",
                ccdSelfEmployed.getReason(), actual.getReason());
        }

        if (!Objects.equals(actual.getAmountYouOwe(), ccdSelfEmployed.getAmountYouOwe())) {
            failWithMessage("Expected SelfEmployed.amountYouOwe to be <%s> but was <%s>",
                ccdSelfEmployed.getAmountYouOwe(), actual.getAmountYouOwe());
        }


        return this;
    }

}
