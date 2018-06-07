package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDEmployer {
    private String jobTitle;
    private String employerName;
}
