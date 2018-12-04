package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class InterestDateMixIn {

    @JsonProperty("interestDateType")
    abstract InterestDate.InterestDateType getType();

    @JsonProperty("interestClaimStartDate")
    abstract LocalDate getDate();

    @JsonProperty("interestStartDateReason")
    abstract String getReason();

    @JsonProperty("interestEndDateType")
    abstract InterestDate.InterestEndDateType getEndDateType();
}
