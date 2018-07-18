package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
public class CaseReference {
    @JsonProperty("case_reference")
    private String caseReference;

    @JsonCreator
    public CaseReference(String reference) {
        this.caseReference = reference;
    }

    public String getCaseReference() {
        return caseReference;
    }

    public void setCaseReference(String caseReference) {
        this.caseReference = caseReference;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
