package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class Witness {

    @NotNull
    private final YesNoOption selfWitness;

    @Valid
    @Min(1)
    @Max(100)
    private final Integer noOfOtherWitness;

    @Builder
    public Witness(YesNoOption selfWitness, Integer noOfOtherWitness) {
        this.selfWitness = selfWitness;
        this.noOfOtherWitness = noOfOtherWitness;
    }

    public Optional<Integer> getNoOfOtherWitness() {
        return Optional.ofNullable(noOfOtherWitness);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
