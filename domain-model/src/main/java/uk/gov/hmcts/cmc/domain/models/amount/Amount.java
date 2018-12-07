package uk.gov.hmcts.cmc.domain.models.amount;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = AmountBreakDown.class, name = "breakdown"),
        @JsonSubTypes.Type(value = AmountRange.class, name = "range"),
        @JsonSubTypes.Type(value = NotKnown.class, name = "not_known")
    }
)
@EqualsAndHashCode
public abstract class Amount {

    private String type;

    public Amount(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
