package uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;

import java.util.List;

@Builder
@Value
public class CCDEmployment {
    private List<CCDCollectionElement<CCDEmployer>> employers;
    private CCDSelfEmployment selfEmployment;
    private CCDUnemployment unemployment;
}
