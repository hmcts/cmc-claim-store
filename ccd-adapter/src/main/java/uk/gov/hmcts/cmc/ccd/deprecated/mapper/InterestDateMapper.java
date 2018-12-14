package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDInterestDate;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDInterestDateType;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDInterestEndDateType;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.InterestDate.InterestEndDateType;

//@Component
public class InterestDateMapper implements Mapper<CCDInterestDate, InterestDate> {

    @Override
    public CCDInterestDate to(InterestDate interestDate) {
        if (interestDate == null || interestDate.getType() == null) {
            return null;
        }

        CCDInterestDate.CCDInterestDateBuilder builder = CCDInterestDate.builder();
        return builder.type(CCDInterestDateType.valueOf(interestDate.getType().name()))
            .date(interestDate.getDate())
            .reason(interestDate.getReason())
            .endDateType(CCDInterestEndDateType.valueOf(interestDate.getEndDateType().name()))
            .build();
    }

    @Override
    public InterestDate from(CCDInterestDate ccdInterestDate) {
        if (ccdInterestDate == null) {
            return null;
        }

        InterestEndDateType endDateType = ccdInterestDate.getEndDateType() != null
            ? InterestEndDateType.valueOf(ccdInterestDate.getEndDateType().name())
            : null;

        return new InterestDate(
            InterestDate.InterestDateType.valueOf(ccdInterestDate.getType().name()),
            ccdInterestDate.getDate(),
            ccdInterestDate.getReason(),
            endDateType
        );
    }
}
