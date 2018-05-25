package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDChildren;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Children;

@Component
public class ChildrenMapper implements Mapper<CCDChildren, Children> {

    @Override
    public CCDChildren to(Children children) {
        return CCDChildren.builder()
            .under11(children.getUnder11().orElse(0))
            .between11and15(children.getBetween11and15().orElse(0))
            .between16and19(children.getBetween16and19().orElse(0))
            .build();
    }

    @Override
    public Children from(CCDChildren ccdChildren) {
        return new Children(
            ccdChildren.getUnder11(),
            ccdChildren.getBetween11and15(),
            ccdChildren.getBetween16and19()
        );
    }
}
