package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDSoleTrader;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class SoleTraderDetailsAssert extends AbstractAssert<SoleTraderDetailsAssert, SoleTraderDetails> {

    public SoleTraderDetailsAssert(SoleTraderDetails soleTrader) {
        super(soleTrader, SoleTraderDetailsAssert.class);
    }

    public SoleTraderDetailsAssert isEqualTo(CCDSoleTrader ccdSoleTrader) {
        isNotNull();

        actual.getTitle().ifPresent(title -> assertThat(ccdSoleTrader.getTitle()).isEqualTo(title));

        if (!Objects.equals(actual.getName(), ccdSoleTrader.getName())) {
            failWithMessage("Expected CCDSoleTrader.name to be <%s> but was <%s>",
                ccdSoleTrader.getName(), actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), ccdSoleTrader.getEmail())) {
            failWithMessage("Expected CCDSoleTrader.email to be <%s> but was <%s>",
                ccdSoleTrader.getEmail(), actual.getEmail().orElse(null));
        }

        if (!Objects.equals(actual.getBusinessName().orElse(null), ccdSoleTrader.getBusinessName())) {
            failWithMessage("Expected CCDSoleTrader.businessName to be <%s> but was <%s>",
                ccdSoleTrader.getBusinessName(), actual.getBusinessName().orElse(null));
        }

        assertThat(ccdSoleTrader.getAddress()).isEqualTo(actual.getAddress());
        actual.getServiceAddress()
            .ifPresent(address -> assertThat(ccdSoleTrader.getCorrespondenceAddress())
                .isEqualTo(address));
        actual.getRepresentative()
            .ifPresent(representative -> assertThat(ccdSoleTrader.getRepresentative())
                .isEqualTo(representative));

        return this;
    }
}
