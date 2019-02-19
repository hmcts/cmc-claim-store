package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@Getter
public class ReDetermination {

    @NotNull
    private final String explanation;

    @NotNull
    private final MadeBy partyType;

    @Builder
    public ReDetermination(String explanation, MadeBy partyType) {
        this.explanation = explanation;
        this.partyType = partyType;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
