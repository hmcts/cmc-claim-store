package uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CCDExpertReport {
    private String expertName;
    private LocalDate expertReportDate;
}
