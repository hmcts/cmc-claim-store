package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.util.List;

@Value
@Builder
public class CCDEmployment {
    private CCDYesNoOption isEmployed;
    private CCDYesNoOption isSelfEmployed;
    private List<CCDCollectionElement<CCDEmployer>> employers;
    private CCDSelfEmployed selfEmployed;
    private CCDUnEmployed unEmployed;
}
