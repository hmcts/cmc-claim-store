package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDDependant;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;

@Component
public class DependantMapper implements Mapper<CCDDependant, Dependant> {

    @Override
    public CCDDependant to(Dependant dependant) {
        return CCDDependant.builder()
            .children(dependant.getChildren().orElse(null))
            .maintainedChildren(dependant.getMaintainedChildren().orElse(0))
            .build();
    }

    @Override
    public Dependant from(CCDDependant ccdDependant) {
        if (ccdDependant == null) {
            return null;
        }

        return new Dependant(
            ccdDependant.getChildren(),
            ccdDependant.getMaintainedChildren()
        );
    }
}
