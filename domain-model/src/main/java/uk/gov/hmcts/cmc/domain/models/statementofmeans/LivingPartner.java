package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
@Getter
public class LivingPartner {

    @Valid
    @NotNull
    private final DisabilityStatus disability;

    private final boolean over18;

    private final boolean pensioner;

    public LivingPartner(DisabilityStatus disability, boolean over18, boolean pensioner) {
        this.over18 = over18;
        this.disability = disability;
        this.pensioner = pensioner;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
