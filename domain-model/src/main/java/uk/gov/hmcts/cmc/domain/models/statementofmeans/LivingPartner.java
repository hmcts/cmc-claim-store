package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class LivingPartner {

    private final boolean declared;
    private final boolean adult;
    private final DisabilityStatus disability;

    public LivingPartner(boolean declared, boolean adult, DisabilityStatus disability) {
        this.declared = declared;
        this.adult = adult;
        this.disability = disability;
    }

    public boolean isDeclared() {
        return declared;
    }

    public boolean isAdult() {
        return adult;
    }

    public DisabilityStatus getDisability() {
        return disability;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
