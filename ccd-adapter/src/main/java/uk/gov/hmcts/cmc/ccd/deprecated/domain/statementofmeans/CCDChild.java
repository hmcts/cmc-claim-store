package uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

@Builder
@Value
public class CCDChild {
    private Child.AgeGroupType ageGroupType;
    private Integer numberOfChildren;
    private Integer numberOfChildrenLivingWithYou;
}
