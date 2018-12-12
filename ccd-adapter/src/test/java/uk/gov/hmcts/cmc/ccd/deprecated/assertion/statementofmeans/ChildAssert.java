package uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDChild;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

import java.util.Objects;

public class ChildAssert extends AbstractAssert<ChildAssert, Child> {

    public ChildAssert(Child actual) {
        super(actual, ChildAssert.class);
    }

    public ChildAssert isEqualTo(CCDChild ccdChild) {
        isNotNull();

        if (!Objects.equals(actual.getNumberOfChildren(), ccdChild.getNumberOfChildren())) {
            failWithMessage("Expected Child.numberOfChildren to be <%s> but was <%s>",
                ccdChild.getNumberOfChildren(), actual.getNumberOfChildren());
        }

        if (!Objects.equals(actual.getAgeGroupType(), ccdChild.getAgeGroupType())) {
            failWithMessage("Expected Child.ageGroupType to be <%s> but was <%s>",
                ccdChild.getAgeGroupType(), actual.getAgeGroupType());
        }

        if (!Objects.equals(actual.getNumberOfChildrenLivingWithYou().orElse(null),
            ccdChild.getNumberOfChildrenLivingWithYou())
            ) {
            failWithMessage("Expected Child.numberOfChildrenLivingWithYou to be <%s> but was <%s>",
                ccdChild.getNumberOfChildrenLivingWithYou(), actual.getNumberOfChildrenLivingWithYou());
        }

        return this;
    }

}
