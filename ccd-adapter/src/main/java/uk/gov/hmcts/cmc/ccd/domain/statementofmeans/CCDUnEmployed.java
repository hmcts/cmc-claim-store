package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDUnEmployed {
    private String type;
    private Integer noOfYears;
    private Integer noOfMonths;
}
