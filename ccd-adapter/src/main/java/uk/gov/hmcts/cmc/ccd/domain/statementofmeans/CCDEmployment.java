package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployed;

import java.util.List;

@Value
@Builder
public class CCDEmployment {
    private YesNoOption isEmployed;
    private YesNoOption isSelfEmployed;
    private List<Employer> employers;
    private SelfEmployed selfEmployed;
}
