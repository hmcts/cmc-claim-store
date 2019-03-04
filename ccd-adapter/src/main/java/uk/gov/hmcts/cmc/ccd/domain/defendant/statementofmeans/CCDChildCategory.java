package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDChildCategory {
    private CCDAgeGroupType ageGroupType;
    private Integer numberOfChildren;
    private Integer numberOfResidentChildren;
}
