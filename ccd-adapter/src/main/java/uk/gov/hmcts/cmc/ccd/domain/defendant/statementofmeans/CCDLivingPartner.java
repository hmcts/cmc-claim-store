package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

@Value
@Builder
public class CCDLivingPartner {
    private CCDDisabilityStatus disability;

    private CCDYesNoOption over18;

    private CCDYesNoOption pensioner;
}
