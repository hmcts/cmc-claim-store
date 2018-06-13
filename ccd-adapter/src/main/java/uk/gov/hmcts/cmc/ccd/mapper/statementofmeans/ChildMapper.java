package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDChild;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

@Component
public class ChildMapper implements Mapper<CCDChild, Child> {

    @Override
    public CCDChild to(Child child) {
        if (child == null) {
            return null;
        }

        return CCDChild.builder()
            .ageGroupType(CCDChild.AgeGroupType.valueOf(child.getAgeGroupType().name()))
            .numberOfChildren(child.getNumberOfChildren())
            .numberOfChildrenLivingWithYou(child.getNumberOfChildrenLivingWithYou().orElse(0))
            .build();
    }

    @Override
    public Child from(CCDChild ccdChild) {
        if (ccdChild == null) {
            return null;
        }

        return Child.builder()
            .ageGroupType(Child.AgeGroupType.valueOf(ccdChild.getAgeGroupType().name()))
            .numberOfChildren(ccdChild.getNumberOfChildren())
            .numberOfChildrenLivingWithYou(ccdChild.getNumberOfChildrenLivingWithYou())
            .build();
    }
}
