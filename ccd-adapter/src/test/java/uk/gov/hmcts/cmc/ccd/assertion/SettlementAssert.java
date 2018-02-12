package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDSettlement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;

import static org.assertj.core.api.Assertions.assertThat;

public class SettlementAssert extends AbstractAssert<SettlementAssert, Settlement> {

    public SettlementAssert(Settlement actual) {
        super(actual, SettlementAssert.class);
    }

    public SettlementAssert isEqualTo(CCDSettlement ccdSettlement) {
        isNotNull();

        assertThat(actual.getPartyStatements().size()).isEqualTo(ccdSettlement.getPartyStatements().size());

        return this;
    }
}
