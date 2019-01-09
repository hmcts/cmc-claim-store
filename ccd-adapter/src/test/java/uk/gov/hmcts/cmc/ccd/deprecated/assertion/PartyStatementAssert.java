package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.offers.CCDPartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class PartyStatementAssert extends AbstractAssert<PartyStatementAssert, PartyStatement> {

    public PartyStatementAssert(PartyStatement actual) {
        super(actual, PartyStatementAssert.class);
    }

    public PartyStatementAssert isEqualTo(CCDPartyStatement ccdPartyStatement) {
        isNotNull();

        if (!Objects.equals(actual.getMadeBy().name(), ccdPartyStatement.getMadeBy().name())) {
            failWithMessage("Expected Party Statement.made by to be <%s> but was <%s>",
                ccdPartyStatement.getMadeBy(), actual.getMadeBy().name());
        }

        if (!Objects.equals(actual.getType().name(), ccdPartyStatement.getType().name())) {
            failWithMessage("Expected Party Statement.type to be <%s> but was <%s>",
                ccdPartyStatement.getType().name(), actual.getType().name());
        }

        actual.getOffer().ifPresent(offer -> assertThat(offer).isEqualTo(ccdPartyStatement.getOffer()));

        return this;
    }
}
