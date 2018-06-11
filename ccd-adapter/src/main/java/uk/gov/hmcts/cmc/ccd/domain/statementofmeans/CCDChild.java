package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CCDChild {

    private AgeGroupType ageGroupType;
    private Integer numberOfChildren;
    private Integer numberOfChildrenLivingWithYou;

    public enum AgeGroupType {
        UNDER_11,
        BETWEEN_11_AND_15,
        BETWEEN_16_AND_19;
    }
}
