package uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.DisabilityStatus;

@Value
@Builder
public class CCDLivingPartner {
    private DisabilityStatus disability;

    private CCDYesNoOption over18;

    private CCDYesNoOption pensioner;
}
