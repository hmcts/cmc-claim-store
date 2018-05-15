package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDate;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class InterestDateAssert extends AbstractAssert<InterestDateAssert, InterestDate> {

    public InterestDateAssert(InterestDate actual) {
        super(actual, InterestDateAssert.class);
    }

    public InterestDateAssert isEqualTo(CCDInterestDate ccdInterestDate) {

        if (actual == null) {
            assertThat(ccdInterestDate).isNull();
        } else {
            if (!Objects.equals(actual.getDate(), ccdInterestDate.getDate())) {
                failWithMessage("Expected InterestDate.date to be <%s> but was <%s>",
                    ccdInterestDate.getDate(), actual.getDate());
            }

            if (!Objects.equals(actual.getReason(), ccdInterestDate.getReason())) {
                failWithMessage("Expected InterestDate.reason to be <%s> but was <%s>",
                    ccdInterestDate.getReason(), actual.getReason());
            }

            if (!Objects.equals(actual.getType().name(), ccdInterestDate.getType().name())) {
                failWithMessage("Expected InterestDate.type to be <%s> but was <%s>",
                    ccdInterestDate.getType().name(), actual.getType().name());
            }
        }

        return this;
    }

}
