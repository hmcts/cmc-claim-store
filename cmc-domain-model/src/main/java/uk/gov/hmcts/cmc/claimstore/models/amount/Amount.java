package uk.gov.hmcts.cmc.claimstore.models.amount;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = AmountBreakDown.class, name = "breakdown"),
        @JsonSubTypes.Type(value = AmountRange.class, name = "range"),
        @JsonSubTypes.Type(value = NotKnown.class, name = "not_known")
    }
)
public interface Amount {

}
