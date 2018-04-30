package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDSoleTrader;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class SoleTraderAssert extends AbstractAssert<SoleTraderAssert, SoleTrader> {

    public SoleTraderAssert(SoleTrader soleTrader) {
        super(soleTrader, SoleTraderAssert.class);
    }

    public SoleTraderAssert isEqualTo(CCDSoleTrader ccdSoleTrader) {
        isNotNull();

        actual.getTitle().ifPresent(title -> assertThat(ccdSoleTrader.getTitle()).isEqualTo(title));

        if (!Objects.equals(actual.getName(), ccdSoleTrader.getName())) {
            failWithMessage("Expected CCDSoleTrader.name to be <%s> but was <%s>",
                ccdSoleTrader.getName(), actual.getName());
        }

        if (!Objects.equals(actual.getPhoneNumber().orElse(null), ccdSoleTrader.getPhoneNumber())) {
            failWithMessage("Expected CCDSoleTrader.phoneNumber to be <%s> but was <%s>",
                ccdSoleTrader.getPhoneNumber(), actual.getPhoneNumber().orElse(null));
        }

        if (!Objects.equals(actual.getBusinessName().orElse(null), ccdSoleTrader.getBusinessName())) {
            failWithMessage("Expected CCDSoleTrader.businessName to be <%s> but was <%s>",
                ccdSoleTrader.getBusinessName(), actual.getBusinessName().orElse(null));
        }

        assertThat(ccdSoleTrader.getAddress()).isEqualTo(actual.getAddress());
        actual.getCorrespondenceAddress()
            .ifPresent(address -> assertThat(ccdSoleTrader.getCorrespondenceAddress())
                .isEqualTo(address));
        actual.getRepresentative()
            .ifPresent(representative -> assertThat(ccdSoleTrader.getRepresentative())
                .isEqualTo(representative));

        return this;
    }
}
