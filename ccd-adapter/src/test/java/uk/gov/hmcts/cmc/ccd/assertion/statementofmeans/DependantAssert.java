package uk.gov.hmcts.cmc.ccd.assertion.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDDependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class DependantAssert extends AbstractAssert<DependantAssert, Dependant> {

    public DependantAssert(Dependant actual) {
        super(actual, DependantAssert.class);
    }

    public DependantAssert isEqualTo(CCDDependant ccdDependant) {
        isNotNull();

        assertThat(actual.getChildren().orElse(null)).isEqualTo(ccdDependant.getChildren());

        if (!Objects.equals(actual.getMaintainedChildren().orElse(0), ccdDependant.getMaintainedChildren())) {
            failWithMessage("Expected Dependant.maintainedChildren to be <%s> but was <%s>",
                ccdDependant.getMaintainedChildren(), actual.getMaintainedChildren());
        }

        return this;
    }

}
