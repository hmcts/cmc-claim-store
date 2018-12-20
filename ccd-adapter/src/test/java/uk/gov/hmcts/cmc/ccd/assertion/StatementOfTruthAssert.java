package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.util.Objects;

public class StatementOfTruthAssert extends AbstractAssert<StatementOfTruthAssert, StatementOfTruth> {

    public StatementOfTruthAssert(StatementOfTruth statementOfTruth) {
        super(statementOfTruth, StatementOfTruthAssert.class);
    }

    public StatementOfTruthAssert isEqualTo(CCDStatementOfTruth ccdStatementOfTruth) {
        isNotNull();

        if (!Objects.equals(actual.getSignerName(), ccdStatementOfTruth.getSignerName())) {
            failWithMessage("Expected CCDStatementOfTruth.signerName to be <%s> but was <%s>",
                ccdStatementOfTruth.getSignerName(), actual.getSignerName());
        }

        if (!Objects.equals(actual.getSignerRole(), ccdStatementOfTruth.getSignerRole())) {
            failWithMessage("Expected CCDStatementOfTruth.signerRole to be <%s> but was <%s>",
                ccdStatementOfTruth.getSignerRole(), actual.getSignerRole());
        }

        return this;
    }

}
