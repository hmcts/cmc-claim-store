package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
@Getter
public class OtherDependants {

    @NotNull
    @Min(1)
    private final Integer numberOfPeople;

    @NotBlank
    private final String details;

    private final boolean anyDisabled;

    public OtherDependants(Integer numberOfPeople, String details, boolean anyDisabled) {
        this.numberOfPeople = numberOfPeople;
        this.details = details;
        this.anyDisabled = anyDisabled;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
