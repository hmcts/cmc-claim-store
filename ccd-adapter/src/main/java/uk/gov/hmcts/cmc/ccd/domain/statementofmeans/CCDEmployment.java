package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;

import java.util.List;

@Builder
@Value
public class CCDEmployment {
    private List<CCDCollectionElement<CCDEmployer>> employers;
    private SelfEmployment selfEmployment;
    private Unemployment unemployment;
}
