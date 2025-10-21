package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class StatementOfMeansAssert extends CustomAssert<StatementOfMeansAssert, StatementOfMeans> {

    public StatementOfMeansAssert(StatementOfMeans actual) {
        super("StatementOfMeans", actual, StatementOfMeansAssert.class);
    }

    public StatementOfMeansAssert isEqualTo(CCDStatementOfMeans expected) {
        isNotNull();

        compare("reason",
            expected.getReason(),
            Optional.ofNullable(actual.getReason()));

        compareCollections(
            expected.getCourtOrders(), actual.getCourtOrders(),
            CCDCourtOrder::getClaimNumber, CourtOrder::getClaimNumber,
            (e, a) -> assertThat(a).isEqualTo(e)
        );

        compareCollections(
            expected.getBankAccounts(), actual.getBankAccounts(),
            e -> e.getType().name(), a -> a.getType().name(),
            (e, a) -> assertThat(a).isEqualTo(e)
        );

        compareCollections(
            expected.getDebts(), actual.getDebts(),
            CCDDebt::getDescription, Debt::getDescription,
            (e, a) -> assertThat(a).isEqualTo(e)
        );

        compareCollections(
            expected.getIncomes(), actual.getIncomes(),
            e -> e.getType().name(), a -> a.getType().name(),
            (e, a) -> assertThat(a).isEqualTo(e)
        );

        compareCollections(
            expected.getExpenses(), actual.getExpenses(),
            e -> e.getType().name(), a -> a.getType().name(),
            (e, a) -> assertThat(a).isEqualTo(e)
        );

        compareCollections(
            expected.getPriorityDebts(), actual.getPriorityDebts(),
            e -> e.getType().name(), a -> a.getType().name(),
            (e, a) -> assertThat(a).isEqualTo(e)
        );

        compare("partner",
            expected.getLivingPartner(),
            actual.getPartner(),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("carer",
            expected.getCarer(), CCDYesNoOption::toBoolean,
            Optional.of(actual.isCarer()));

        compare("disability",
            expected.getDisabilityStatus(), Enum::name,
            actual.getDisability().map(Enum::name));

        return this;
    }
}
