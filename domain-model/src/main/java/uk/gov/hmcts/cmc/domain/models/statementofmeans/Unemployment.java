package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Optional;
import javax.validation.Valid;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class Unemployment {

    @Valid
    private final Unemployed unemployed;

    private final boolean retired;
    private final String other;

    public Unemployment(Unemployed unemployed, boolean retired, String other) {
        this.unemployed = unemployed;
        this.retired = retired;
        this.other = other;
    }

    public Optional<Unemployed> getUnemployed() {
        return Optional.ofNullable(unemployed);
    }

    public boolean isRetired() {
        return retired;
    }

    public Optional<String> getOther() {
        return Optional.ofNullable(other);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
