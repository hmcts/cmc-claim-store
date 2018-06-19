package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

@Builder
@Value
public class CCDUnemployment {
    private CCDUnemployed unemployed;
    private CCDYesNoOption retired;
    private String other;
}
