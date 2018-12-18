package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDEmployment {
    private String jobTitle;
    private String employerName;
}
