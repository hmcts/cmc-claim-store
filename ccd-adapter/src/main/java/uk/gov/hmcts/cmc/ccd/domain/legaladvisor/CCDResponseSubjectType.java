package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CCDResponseSubjectType {
    RES_CLAIMANT("Res_CLAIMANT"),
    RES_DEFENDANT("Res_DEFENDANT"),
    NONE("None");

    private final String value;

    CCDResponseSubjectType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
