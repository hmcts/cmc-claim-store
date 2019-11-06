package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class CourtOrderAssert extends CustomAssert<CourtOrderAssert, CourtOrder> {

    public CourtOrderAssert(CourtOrder actual) {
        super("CourtOrder", actual, CourtOrderAssert.class);
    }

    public CourtOrderAssert isEqualTo(CCDCourtOrder expected) {
        isNotNull();

        compare("claimNumber",
            expected.getClaimNumber(),
            Optional.ofNullable(actual.getClaimNumber()));

        compare("amountOwed",
            expected.getAmountOwed(),
            Optional.ofNullable(actual.getAmountOwed()),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("monthlyInstalmentAmount",
            expected.getMonthlyInstalmentAmount(),
            Optional.ofNullable(actual.getMonthlyInstalmentAmount()),
            (e, a) -> assertMoney(a).isEqualTo(e));

        return this;
    }

}
