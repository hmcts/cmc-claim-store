package uk.gov.hmcts.cmc.ccd.assertion.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDChildren;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Children;

import java.util.Objects;

public class ChildrenAssert extends AbstractAssert<ChildrenAssert, Children> {

    public ChildrenAssert(Children actual) {
        super(actual, ChildrenAssert.class);
    }

    public ChildrenAssert isEqualTo(CCDChildren ccdChildren) {
        isNotNull();

        if (!Objects.equals(actual.getUnder11().orElse(0), ccdChildren.getUnder11())) {
            failWithMessage("Expected Children.under11 to be <%s> but was <%s>",
                ccdChildren.getUnder11(), actual.getUnder11().orElse(0));
        }

        if (!Objects.equals(actual.getBetween11and15().orElse(0), ccdChildren.getBetween11and15())) {
            failWithMessage("Expected Children.between11and15 to be <%s> but was <%s>",
                ccdChildren.getBetween11and15(), actual.getBetween11and15());
        }

        if (!Objects.equals(actual.getBetween16and19().orElse(0), ccdChildren.getBetween16and19())) {
            failWithMessage("Expected Children.between16and19 to be <%s> but was <%s>",
                ccdChildren.getBetween16and19(), actual.getBetween16and19());
        }

        return this;
    }

}
