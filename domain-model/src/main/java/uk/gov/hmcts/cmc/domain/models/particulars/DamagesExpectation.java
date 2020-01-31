package uk.gov.hmcts.cmc.domain.models.particulars;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DamagesExpectation {
    @JsonProperty("moreThanThousandPounds")
    MORE_THAN_THOUSAND_POUNDS("more than"),
    @JsonProperty("thousandPoundsOrLess")
    THOUSAND_POUNDS_OR_LESS("not more than");

    private final String displayValue;

    DamagesExpectation(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
