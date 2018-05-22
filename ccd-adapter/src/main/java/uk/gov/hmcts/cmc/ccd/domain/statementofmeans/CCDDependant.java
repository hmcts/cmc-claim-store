package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Children;

@Value
@Builder
public class CCDDependant {
    private Children children;
    private Integer maintainedChildren;
}
