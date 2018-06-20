package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CCDUnemployed {
    private Integer numberOfYears;
    private Integer numberOfMonths;
}
