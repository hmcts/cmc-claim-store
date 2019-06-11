package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
public class Evidence {

    @NotNull
    private final YesNoOption selfEvidence;
    @Min(1)
    private final Integer noOfOtherPeopleEvidence;

    @Builder
    public Evidence(YesNoOption selfEvidence, Integer noOfOtherPeopleEvidence) {
        this.selfEvidence = selfEvidence;
        this.noOfOtherPeopleEvidence = noOfOtherPeopleEvidence;
    }

    public Optional<Integer> getNoOfOtherPeopleEvidence() {
        return Optional.ofNullable(noOfOtherPeopleEvidence);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
