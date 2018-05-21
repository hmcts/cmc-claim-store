package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CCDInterestDate {
    private CCDInterestDateType type;
    private LocalDate date;
    private String reason;
    private CCDInterestEndDateType endDateType;
}
