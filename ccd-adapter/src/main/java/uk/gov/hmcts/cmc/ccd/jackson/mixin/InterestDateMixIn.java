package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.time.LocalDate;

public interface InterestDateMixIn {

    @JsonProperty("interestDateType")
     InterestDate.InterestDateType getType();

    @JsonProperty("interestClaimStartDate")
     LocalDate getDate();

    @JsonProperty("interestStartDateReason")
     String getReason();

    @JsonProperty("interestEndDateType")
     InterestDate.InterestEndDateType getEndDateType();
}
