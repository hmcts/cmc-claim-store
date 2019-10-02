package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class ExpertRequest {

    @Valid
    @Size(max = 1000)
    private final String expertEvidenceToExamine;

    @Valid
    @Size(max = 99000)
    private final String reasonForExpertAdvice;
    @NotNull
    private final YesNoOption expertRequired;

    @Builder
    public ExpertRequest(
        YesNoOption expertRequired,
        String expertEvidenceToExamine,
        String reasonForExpertAdvice
    ) {
        this.expertRequired = expertRequired;
        this.expertEvidenceToExamine = expertEvidenceToExamine;
        this.reasonForExpertAdvice = reasonForExpertAdvice;
    }

    public Optional<String> getExpertEvidenceToExamine() {
        return Optional.ofNullable(expertEvidenceToExamine);
    }

    public Optional<String> getReasonForExpertAdvice() {
        return Optional.ofNullable(reasonForExpertAdvice);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
