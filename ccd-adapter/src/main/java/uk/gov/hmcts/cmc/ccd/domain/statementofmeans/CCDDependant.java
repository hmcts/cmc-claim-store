package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDDependant {
    private CCDChildren children;
    private Integer maintainedChildren;
}
