package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        CaseReference obj = (CaseReference) other;

        return caseReference != null && caseReference.equals(obj.caseReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseReference);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
