package uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDChild;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDDependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class DependantAssert extends AbstractAssert<DependantAssert, Dependant> {

    public DependantAssert(Dependant actual) {
        super(actual, DependantAssert.class);
    }

    public DependantAssert isEqualTo(CCDDependant ccdDependant) {
        isNotNull();


        if (!Objects.equals(actual.getNumberOfMaintainedChildren().orElse(null),
            ccdDependant.getNumberOfMaintainedChildren())
            ) {
            failWithMessage("Expected Dependant.numberOfMaintainedChildren to be <%s> but was <%s>",
                ccdDependant.getNumberOfMaintainedChildren(), actual.getNumberOfMaintainedChildren());
        }

        actual.getChildren()
            .forEach(child -> assertChildren(child, ccdDependant.getChildren()));

        actual.getOtherDependants().ifPresent(otherDependants -> {
            if (!Objects.equals(otherDependants.getNumberOfPeople(),
                ccdDependant.getOtherDependants().getNumberOfPeople())) {
                failWithMessage("Expected OtherDependants.numberOfPeople to be <%s> but was <%s>",
                    ccdDependant.getOtherDependants().getNumberOfPeople(), otherDependants.getNumberOfPeople());
            }

            if (!Objects.equals(otherDependants.getDetails(),
                ccdDependant.getOtherDependants().getDetails())) {
                failWithMessage("Expected OtherDependants.details to be <%s> but was <%s>",
                    ccdDependant.getOtherDependants().getDetails(), otherDependants.getDetails());
            }
        });

        return this;
    }

    private void assertChildren(
        Child actual,
        List<CCDCollectionElement<CCDChild>> ccdChildren
    ) {
        ccdChildren.stream()
            .map(CCDCollectionElement::getValue)
            .filter(ccdChild -> actual.getAgeGroupType().name().equals(ccdChild.getAgeGroupType().name()))
            .findFirst()
            .ifPresent(child -> assertThat(actual).isEqualTo(child));
    }
}
