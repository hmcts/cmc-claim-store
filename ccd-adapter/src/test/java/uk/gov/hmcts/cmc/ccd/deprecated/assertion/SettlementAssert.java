package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.offers.CCDSettlement;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class SettlementAssert extends AbstractAssert<SettlementAssert, Settlement> {

    public SettlementAssert(Settlement actual) {
        super(actual, SettlementAssert.class);
    }

    public SettlementAssert isEqualTo(CCDSettlement ccdSettlement) {
        isNotNull();

        assertThat(actual.getPartyStatements().size()).isEqualTo(ccdSettlement.getPartyStatements().size());

        actual.getPartyStatements()
            .forEach(partyStatement -> assertPartyStatement(partyStatement, ccdSettlement.getPartyStatements()));

        return this;
    }

    private void assertPartyStatement(
        PartyStatement actual,
        List<CCDCollectionElement<CCDPartyStatement>> ccdPartyStatements
    ) {
        ccdPartyStatements.stream()
            .map(CCDCollectionElement::getValue)
            .filter(partyStatement -> actual.getType().name().equals(partyStatement.getType().name()))
            .findFirst()
            .ifPresent(partyStatement -> assertThat(actual).isEqualTo(partyStatement));
    }
}
