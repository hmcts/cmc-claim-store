package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDChildCategory;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

import java.util.Optional;

public class ChildAssert extends CustomAssert<ChildAssert, Child> {

    public ChildAssert(Child actual) {
        super("Child", actual, ChildAssert.class);
    }

    public ChildAssert isEqualTo(CCDChildCategory expected) {
        isNotNull();

        compare("numberOfChildren",
            expected.getNumberOfChildren(),
            Optional.ofNullable(actual.getNumberOfChildren()));

        compare("ageGroupType",
            expected.getAgeGroupType(), Enum::name,
            Optional.ofNullable(actual.getAgeGroupType()).map(Enum::name));

        compare("numberOfChildrenLivingWithYou",
            expected.getNumberOfResidentChildren(),
            actual.getNumberOfChildrenLivingWithYou());

        return this;
    }

}
