package uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDChild;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

@Component
public class ChildMapper implements Mapper<CCDChild, Child> {

    @Override
    public CCDChild to(Child child) {

        return CCDChild.builder()
            .ageGroupType(child.getAgeGroupType())
            .numberOfChildren(child.getNumberOfChildren())
            .numberOfChildrenLivingWithYou(child.getNumberOfChildrenLivingWithYou().orElse(null))
            .build();
    }

    @Override
    public Child from(CCDChild ccdChild) {
        if (ccdChild == null) {
            return null;
        }

        return Child.builder()
            .ageGroupType(ccdChild.getAgeGroupType())
            .numberOfChildren(ccdChild.getNumberOfChildren())
            .numberOfChildrenLivingWithYou(ccdChild.getNumberOfChildrenLivingWithYou())
            .build();
    }
}
