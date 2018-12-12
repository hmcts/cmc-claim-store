package uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CCDEmployer {
    private String jobTitle;
    private String name;
}
