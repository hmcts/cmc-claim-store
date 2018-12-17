package uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;

import java.util.Objects;

public class CourtOrderAssert extends AbstractAssert<CourtOrderAssert, CourtOrder> {

    public CourtOrderAssert(CourtOrder actual) {
        super(actual, CourtOrderAssert.class);
    }

    public CourtOrderAssert isEqualTo(CCDCourtOrder ccdCourtOrder) {
        isNotNull();

        if (!Objects.equals(actual.getClaimNumber(), ccdCourtOrder.getClaimNumber())) {
            failWithMessage("Expected CourtOrder.claimNumber to be <%s> but was <%s>",
                ccdCourtOrder.getClaimNumber(), actual.getClaimNumber());
        }

        if (!Objects.equals(actual.getAmountOwed(), ccdCourtOrder.getAmountOwed())) {
            failWithMessage("Expected CourtOrder.amountOwed to be <%s> but was <%s>",
                ccdCourtOrder.getAmountOwed(), actual.getAmountOwed());
        }

        if (!Objects.equals(actual.getMonthlyInstalmentAmount(), ccdCourtOrder.getMonthlyInstalmentAmount())) {
            failWithMessage("Expected CourtOrder.monthlyInstalmentAmount to be <%s> but was <%s>",
                ccdCourtOrder.getMonthlyInstalmentAmount(), actual.getMonthlyInstalmentAmount());
        }

        return this;
    }

}
