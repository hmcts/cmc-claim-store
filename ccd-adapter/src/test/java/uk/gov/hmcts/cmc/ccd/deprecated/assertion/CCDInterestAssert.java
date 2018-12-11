package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDInterest;
import uk.gov.hmcts.cmc.domain.models.Interest;

import java.util.Objects;

public class CCDInterestAssert extends AbstractAssert<CCDInterestAssert, CCDInterest> {

    public CCDInterestAssert(CCDInterest actual) {
        super(actual, CCDInterestAssert.class);
    }

    public CCDInterestAssert isEqualTo(Interest interest) {
        isNotNull();

        if (!Objects.equals(actual.getRate(), interest.getRate())) {
            failWithMessage("Expected CCDInterest.rate to be <%s> but was <%s>",
                interest.getRate(), actual.getRate());
        }

        if (!Objects.equals(actual.getReason(), interest.getReason())) {
            failWithMessage("Expected CCDInterest.reason to be <%s> but was <%s>",
                interest.getReason(), actual.getReason());
        }

        if (!Objects.equals(actual.getType().name(), interest.getType().name())) {
            failWithMessage("Expected CCDInterest.type to be <%s> but was <%s>",
                interest.getType().name(), actual.getType().name());
        }

        return this;
    }

}
