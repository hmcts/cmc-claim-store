package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDateType;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestEndDateType;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.InterestDate.InterestEndDateType;

@Component
public class InterestDateMapper implements BuilderMapper<CCDCase, InterestDate, CCDCase.CCDCaseBuilder> {

    @Override
    public void to(InterestDate interestDate, CCDCase.CCDCaseBuilder builder) {
        if (interestDate == null || interestDate.getType() == null) {
            return;
        }

        builder.interestDateType(CCDInterestDateType.valueOf(interestDate.getType().name()))
            .interestClaimStartDate(interestDate.getDate())
            .interestReason(interestDate.getReason())
            .interestEndDateType(CCDInterestEndDateType.valueOf(interestDate.getEndDateType().name()));
    }

    @Override
    public InterestDate from(CCDCase ccdCase) {
        if (ccdCase == null) {
            return null;
        }

        InterestEndDateType endDateType = ccdCase.getInterestDateType() != null
            ? InterestEndDateType.valueOf(ccdCase.getInterestEndDateType().name())
            : null;

        return new InterestDate(
            InterestDate.InterestDateType.valueOf(ccdCase.getInterestDateType().name()),
            ccdCase.getInterestClaimStartDate(),
            ccdCase.getInterestReason(),
            endDateType
        );
    }
}
