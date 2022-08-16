package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Optional;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class DeterminationWithoutHearingQuestions {

    @NotNull
    private final YesNoOption determinationWithoutHearingQuestions;

    private final String determinationWithoutHearingQuestionsDetails;

    @Builder
    public DeterminationWithoutHearingQuestions(YesNoOption determinationWithoutHearingQuestions, String determinationWithoutHearingQuestionsDetails) {
        this.determinationWithoutHearingQuestions = determinationWithoutHearingQuestions;
        this.determinationWithoutHearingQuestionsDetails = determinationWithoutHearingQuestionsDetails;
    }

    public Optional<String> getDeterminationWithoutHearingQuestionsDetails() {
        return Optional.ofNullable(determinationWithoutHearingQuestionsDetails);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
