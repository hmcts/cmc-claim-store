package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDChildCategory;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

@Component
public class ChildCategoryMapper {

    public CCDChildCategory to(Child child) {
        return CCDChildCategory.builder()
            .ageGroupType(child.getAgeGroupType())
            .numberOfChildren(child.getNumberOfChildren())
            .numberOfResidentChildren(child.getNumberOfChildrenLivingWithYou().orElse(null))
            .build();
    }

    public Child from(CCDCollectionElement<CCDChildCategory> ccdChildCategory) {
        CCDChildCategory childCategory = ccdChildCategory.getValue();
        if (childCategory == null) {
            return null;
        }

        return Child.builder()
            .id(ccdChildCategory.getId())
            .ageGroupType(childCategory.getAgeGroupType())
            .numberOfChildren(childCategory.getNumberOfChildren())
            .numberOfChildrenLivingWithYou(childCategory.getNumberOfResidentChildren())
            .build();
    }
}
