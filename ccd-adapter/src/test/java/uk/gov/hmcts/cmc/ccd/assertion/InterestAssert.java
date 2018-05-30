package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterest;
import uk.gov.hmcts.cmc.domain.models.Interest;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class InterestAssert extends AbstractAssert<InterestAssert, Interest> {

    public InterestAssert(Interest actual) {
        super(actual, InterestAssert.class);
    }

    public InterestAssert isEqualTo(CCDInterest ccdInterest) {
        isNotNull();

        if (!Objects.equals(actual.getRate(), ccdInterest.getRate())) {
            failWithMessage("Expected Interest.rate to be <%s> but was <%s>",
                ccdInterest.getRate(), actual.getRate());
        }

        if (!Objects.equals(actual.getReason(), ccdInterest.getReason())) {
            failWithMessage("Expected Interest.reason to be <%s> but was <%s>",
                ccdInterest.getReason(), actual.getReason());
        }

        if (!Objects.equals(actual.getType().name(), ccdInterest.getType().name())) {
            failWithMessage("Expected Interest.type to be <%s> but was <%s>",
                ccdInterest.getType().name(), actual.getType().name());
        }

        if (!Objects.equals(actual.getSpecificDailyAmount().orElse(null), ccdInterest.getSpecificDailyAmount())) {
            failWithMessage("Expected Interest.specificDailyAmount to be <%s> but was <%s>",
                ccdInterest.getSpecificDailyAmount(), actual.getSpecificDailyAmount().orElse(null));
        }

        assertThat(actual.getInterestBreakdown()).isEqualTo(ccdInterest.getInterestBreakdown());
        assertThat(actual.getInterestDate()).isEqualTo(ccdInterest.getInterestDate());

        return this;
    }

}
