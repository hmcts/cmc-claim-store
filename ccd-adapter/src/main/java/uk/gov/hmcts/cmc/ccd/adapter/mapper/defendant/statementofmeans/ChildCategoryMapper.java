package uk.gov.hmcts.cmc.ccd.adapter.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDAgeGroupType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDChildCategory;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

@Component
public class ChildCategoryMapper {

    public CCDCollectionElement<CCDChildCategory> to(Child child) {
        return CCDCollectionElement.<CCDChildCategory>builder()
            .value(CCDChildCategory.builder()
                .ageGroupType(CCDAgeGroupType.valueOf(child.getAgeGroupType().name()))
                .numberOfChildren(child.getNumberOfChildren())
                .numberOfResidentChildren(child.getNumberOfChildrenLivingWithYou().orElse(null))
                .build())
            .id(child.getId())
            .build();
    }

    public Child from(CCDCollectionElement<CCDChildCategory> ccdChildCategory) {
        CCDChildCategory childCategory = ccdChildCategory.getValue();
        if (childCategory == null) {
            return null;
        }

        return Child.builder()
            .id(ccdChildCategory.getId())
            .ageGroupType(Child.AgeGroupType.valueOf(childCategory.getAgeGroupType().name()))
            .numberOfChildren(childCategory.getNumberOfChildren())
            .numberOfChildrenLivingWithYou(childCategory.getNumberOfResidentChildren())
            .build();
    }
}
