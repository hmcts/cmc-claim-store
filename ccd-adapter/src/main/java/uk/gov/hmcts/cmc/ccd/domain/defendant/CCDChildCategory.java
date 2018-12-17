package uk.gov.hmcts.cmc.ccd.domain.defendant;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

@Value
@EqualsAndHashCode
public class CCDChildCategory {
    private Child.AgeGroupType ageGroupType;
    private Integer numberOfChildren;
    private Integer numberOfResidentChildren;
}
