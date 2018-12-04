package uk.gov.hmcts.cmc.ccd.deserialize;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;

import java.time.LocalDate;

public abstract class InterestMixIn {

    @JsonProperty("interestType")
    abstract Interest.InterestType getType();

    @JsonUnwrapped
    abstract InterestBreakdown getInterestBreakdown();
}
