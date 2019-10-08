package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class ExpertRequest {

    @Valid
    @NotNull
    @Size(max = 1000)
    private final String expertEvidenceToExamine;

    @Valid
    @NotNull
    @Size(max = 99000)
    private final String reasonForExpertAdvice;

    @Builder
    public ExpertRequest(String expertEvidenceToExamine, String reasonForExpertAdvice) {
        this.expertEvidenceToExamine = expertEvidenceToExamine;
        this.reasonForExpertAdvice = reasonForExpertAdvice;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
