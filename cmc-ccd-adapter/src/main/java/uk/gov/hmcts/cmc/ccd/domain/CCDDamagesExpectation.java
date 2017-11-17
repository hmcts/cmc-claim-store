package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CCDDamagesExpectation {
    @JsonProperty("moreThanThousandPounds")
    MORE_THAN_THOUSAND_POUNDS,
    @JsonProperty("thousandPoundsOrLess")
    THOUSAND_POUNDS_OR_LESS;
}
