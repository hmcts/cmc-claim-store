package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;

import java.util.List;

@Builder
@Value
public class CCDDependant {
    private List<CCDCollectionElement<CCDChild>> children;
    private Integer numberOfMaintainedChildren;
    private CCDOtherDependants otherDependants;
}
