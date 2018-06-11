package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CCDResidence {

    private ResidenceType type;
    private String otherDetail;

    public enum ResidenceType {
        OWN_HOME,
        JOINT_OWN_HOME,
        PRIVATE_RENTAL,
        COUNCIL_OR_HOUSING_ASSN_HOME,
        OTHER
    }
}
