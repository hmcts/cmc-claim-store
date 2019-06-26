package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

// value will be removed once we change the docmosis template
public enum CCDOtherDirectionHeaderType {
    UPLOAD("HEADER_UPLOAD"),
    CONFIRM("HEADER_CONFIRM");

    private String value;

    CCDOtherDirectionHeaderType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static CCDOtherDirectionHeaderType fromValue(String value) {
        return CCDOtherDirectionHeaderType.valueOf(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

}
