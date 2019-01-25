package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDChildCategory;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

@Component
public class ChildCategoryMapper implements Mapper<CCDChildCategory, Child> {

    @Override
    public CCDChildCategory to(Child child) {
        return CCDChildCategory.builder()
            .ageGroupType(child.getAgeGroupType())
            .numberOfChildren(child.getNumberOfChildren())
            .numberOfResidentChildren(child.getNumberOfChildrenLivingWithYou().orElse(null))
            .build();
    }

    @Override
    public Child from(CCDChildCategory ccdChildCategory) {
        if (ccdChildCategory == null) {
            return null;
        }

        return Child.builder()
            .ageGroupType(ccdChildCategory.getAgeGroupType())
            .numberOfChildren(ccdChildCategory.getNumberOfChildren())
            .numberOfChildrenLivingWithYou(ccdChildCategory.getNumberOfResidentChildren())
            .build();
    }
}
