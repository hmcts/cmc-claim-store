package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDInterestDate;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.util.Objects;

public class CCDInterestDateAssert extends AbstractAssert<CCDInterestDateAssert, CCDInterestDate> {

    public CCDInterestDateAssert(CCDInterestDate actual) {
        super(actual, CCDInterestDateAssert.class);
    }

    public CCDInterestDateAssert isEqualTo(InterestDate interest) {
        isNotNull();

        if (!Objects.equals(actual.getDate(), interest.getDate())) {
            failWithMessage("Expected CCDInterestDate.date to be <%s> but was <%s>",
                interest.getDate(), actual.getDate());
        }

        if (!Objects.equals(actual.getReason(), interest.getReason())) {
            failWithMessage("Expected CCDInterestDate.reason to be <%s> but was <%s>",
                interest.getReason(), actual.getReason());
        }

        if (!Objects.equals(actual.getType().name(), interest.getType().name())) {
            failWithMessage("Expected CCDInterestDate.type to be <%s> but was <%s>",
                interest.getType().name(), actual.getType().name());
        }

        return this;
    }

}
