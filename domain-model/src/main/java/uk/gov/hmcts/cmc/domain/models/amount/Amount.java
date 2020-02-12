package uk.gov.hmcts.cmc.domain.models.amount;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = AmountBreakDown.class, name = "breakdown"),
        @JsonSubTypes.Type(value = AmountRange.class, name = "range"),
        @JsonSubTypes.Type(value = NotKnown.class, name = "not_known")
    }
)
public interface Amount {

}
