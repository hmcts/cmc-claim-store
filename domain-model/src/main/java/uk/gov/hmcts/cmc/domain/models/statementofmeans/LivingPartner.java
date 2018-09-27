package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class LivingPartner {

    private final boolean declared;
    private final boolean over18;
    private final DisabilityStatus disability;
    private final boolean pensioner;

    public LivingPartner(boolean declared, boolean over18, DisabilityStatus disability, boolean pensioner) {
        this.declared = declared;
        this.over18 = over18;
        this.disability = disability;
        this.pensioner = pensioner;
    }

    public boolean isDeclared() {
        return declared;
    }

    public boolean isOver18() {
        return over18;
    }

    public DisabilityStatus getDisability() {
        return disability;
    }

    public boolean isPensioner() {
        return pensioner;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
