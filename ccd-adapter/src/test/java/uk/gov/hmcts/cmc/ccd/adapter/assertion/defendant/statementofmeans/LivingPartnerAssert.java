package uk.gov.hmcts.cmc.ccd.adapter.assertion.defendant.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDLivingPartner;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.LivingPartner;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class LivingPartnerAssert extends AbstractAssert<LivingPartnerAssert, LivingPartner> {

    public LivingPartnerAssert(LivingPartner actual) {
        super(actual, LivingPartnerAssert.class);
    }

    public LivingPartnerAssert isEqualTo(CCDLivingPartner livingPartner) {
        isNotNull();

        if (!Objects.equals(actual.getDisability().name(), livingPartner.getDisability().name())) {
            failWithMessage("Expected LivingPartner.disability to be <%s> but was <%s>",
                livingPartner.getDisability().name(), actual.getDisability().name());
        }

        assertThat(actual.isOver18()).isEqualTo(livingPartner.getOver18().toBoolean());
        assertThat(actual.isPensioner()).isEqualTo(livingPartner.getPensioner().toBoolean());

        return this;
    }
}
