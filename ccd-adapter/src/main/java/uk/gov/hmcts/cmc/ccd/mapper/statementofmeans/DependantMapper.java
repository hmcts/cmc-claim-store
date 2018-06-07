package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDDependant;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;

@Component
public class DependantMapper implements Mapper<CCDDependant, Dependant> {

    private final ChildrenMapper childrenMapper;

    @Autowired
    public DependantMapper(ChildrenMapper childrenMapper) {
        this.childrenMapper = childrenMapper;
    }

    @Override
    public CCDDependant to(Dependant dependant) {
        if (dependant == null) {
            return null;
        }

        return CCDDependant.builder()
            .children(childrenMapper.to(dependant.getChildren().orElse(null)))
            .maintainedChildren(dependant.getMaintainedChildren().orElse(0))
            .build();
    }

    @Override
    public Dependant from(CCDDependant ccdDependant) {
        if (ccdDependant == null) {
            return null;
        }

        return new Dependant(
            childrenMapper.from(ccdDependant.getChildren()),
            ccdDependant.getMaintainedChildren()
        );
    }
}
