package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Interest;

import java.util.Objects;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class InterestAssert extends AbstractAssert<InterestAssert, Interest> {

    public InterestAssert(Interest interest) {
        super(interest, InterestAssert.class);
    }

    public InterestAssert isMappedTo(CCDCase ccdCase) {
        isNotNull();

        ofNullable(actual).ifPresent(interest -> {
                if (!Objects.equals(interest.getRate(), ccdCase.getInterestRate())) {
                    failWithMessage("Expected CCDCase.interestRate to be <%s> but was <%s>",
                        ccdCase.getInterestRate(), interest.getRate());
                }
                if (!Objects.equals(interest.getType().name(), ccdCase.getInterestType().name())) {
                    failWithMessage("Expected CCDCase.interestType to be <%s> but was <%s>",
                        ccdCase.getInterestType(), interest.getType());
                }
                if (!Objects.equals(interest.getReason(), ccdCase.getInterestReason())) {
                    failWithMessage("Expected CCDCase.interestReason to be <%s> but was <%s>",
                        ccdCase.getInterestReason(), interest.getRate());
                }
                interest.getSpecificDailyAmount().ifPresent(dailyAmount -> {
                    assertMoney(dailyAmount)
                        .isEqualTo(ccdCase.getInterestSpecificDailyAmount(),
                            format("Expected CCDCase.interestSpecificDailyAmount to be <%s> but was <%s>",
                                ccdCase.getInterestSpecificDailyAmount(), dailyAmount)
                        );
                });
                ofNullable(interest.getInterestDate()).ifPresent(interestDate -> {
                    if (!Objects.equals(interestDate.getDate(), ccdCase.getInterestClaimStartDate())) {
                        failWithMessage("Expected CCDCase.interestClaimStartDate to be <%s> but was <%s>",
                            ccdCase.getInterestClaimStartDate(), interestDate.getDate());
                    }
                    if (!Objects.equals(interestDate.getType().name(), ccdCase.getInterestDateType().name())) {
                        failWithMessage("Expected CCDCase.interestDateType to be <%s> but was <%s>",
                            ccdCase.getInterestDateType(), interestDate.getType());
                    }

                    if (!Objects.equals(interestDate.getReason(), ccdCase.getInterestStartDateReason())) {
                        failWithMessage("Expected CCDCase.interestStartDateReason to be <%s> but was <%s>",
                            ccdCase.getInterestStartDateReason(), interestDate.getReason());
                    }

                    if (!Objects.equals(interestDate.getEndDateType().name(),
                        ccdCase.getInterestEndDateType().name())
                    ) {
                        failWithMessage("Expected CCDCase.interestEndDateType to be <%s> but was <%s>",
                            ccdCase.getInterestEndDateType(), interestDate.getEndDateType());
                    }
                });
            }
        );
        return this;
    }

}
