package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CCDOtherDependants {
    private Integer numberOfPeople;
    private String details;
}
