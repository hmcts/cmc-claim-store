package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

@Value
@Builder
public class CCDChildCategory {
    private Child.AgeGroupType ageGroupType;
    private Integer numberOfChildren;
    private Integer numberOfResidentChildren;
}
