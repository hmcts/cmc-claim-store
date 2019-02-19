package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDChildCategory;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

import java.util.Objects;

public class ChildAssert extends AbstractAssert<ChildAssert, Child> {

    public ChildAssert(Child actual) {
        super(actual, ChildAssert.class);
    }

    public ChildAssert isEqualTo(CCDChildCategory ccdChildCategory) {
        isNotNull();

        if (!Objects.equals(actual.getNumberOfChildren(), ccdChildCategory.getNumberOfChildren())) {
            failWithMessage("Expected Child.numberOfChildren to be <%s> but was <%s>",
                ccdChildCategory.getNumberOfChildren(), actual.getNumberOfChildren());
        }

        if (!Objects.equals(actual.getAgeGroupType(), ccdChildCategory.getAgeGroupType())) {
            failWithMessage("Expected Child.ageGroupType to be <%s> but was <%s>",
                ccdChildCategory.getAgeGroupType(), actual.getAgeGroupType());
        }

        if (!Objects.equals(actual.getNumberOfChildrenLivingWithYou().orElse(null),
            ccdChildCategory.getNumberOfResidentChildren())
        ) {
            failWithMessage("Expected Child.numberOfChildrenLivingWithYou to be <%s> but was <%s>",
                ccdChildCategory.getNumberOfResidentChildren(), actual.getNumberOfChildrenLivingWithYou());
        }

        return this;
    }

}
