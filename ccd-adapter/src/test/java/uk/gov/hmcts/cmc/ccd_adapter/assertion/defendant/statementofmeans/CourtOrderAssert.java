package uk.gov.hmcts.cmc.ccd_adapter.assertion.defendant.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;

import java.util.Objects;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.ccd_adapter.assertion.Assertions.assertMoney;

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

        assertMoney(actual.getAmountOwed())
            .isEqualTo(
                ccdCourtOrder.getAmountOwed(),
                format("Expected CourtOrder.amountOwed to be <%s> but was <%s>",
                    ccdCourtOrder.getAmountOwed(), actual.getAmountOwed())
            );

        assertMoney(actual.getMonthlyInstalmentAmount())
            .isEqualTo(
                ccdCourtOrder.getMonthlyInstalmentAmount(),
                format("Expected CourtOrder.monthlyInstalmentAmount to be <%s> but was <%s>",
                    ccdCourtOrder.getMonthlyInstalmentAmount(), actual.getMonthlyInstalmentAmount())
            );

        return this;
    }

}
