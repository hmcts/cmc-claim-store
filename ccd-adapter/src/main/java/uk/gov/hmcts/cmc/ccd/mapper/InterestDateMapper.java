package uk.gov.hmcts.cmc.ccd.mapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDateType;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestEndDateType;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.InterestDate.InterestEndDateType;

import java.util.Optional;

@Component
public class InterestDateMapper implements BuilderMapper<CCDCase, InterestDate, CCDCase.CCDCaseBuilder> {

    @Override
    public void to(InterestDate interestDate, CCDCase.CCDCaseBuilder builder) {
        if (interestDate == null) {
            return;
        }
        Optional.ofNullable(interestDate.getType())
            .ifPresent(dateType -> builder.interestDateType(CCDInterestDateType.valueOf(dateType.name())));

        Optional.ofNullable(interestDate.getEndDateType())
            .ifPresent(endDateType -> builder.interestEndDateType(CCDInterestEndDateType.valueOf(endDateType.name())));

        builder
            .interestClaimStartDate(interestDate.getDate())
            .interestStartDateReason(interestDate.getReason());
    }

    @Override
    public InterestDate from(CCDCase ccdCase) {
        if (ccdCase.getInterestDateType() == null
            && ccdCase.getInterestEndDateType() == null
            && ccdCase.getInterestClaimStartDate() == null
            && StringUtils.isBlank(ccdCase.getInterestStartDateReason())
        ) {
            return null;
        }

        InterestEndDateType endDateType = ccdCase.getInterestEndDateType() != null
            ? InterestEndDateType.valueOf(ccdCase.getInterestEndDateType().name())
            : null;

        InterestDate.InterestDateType interestDateType = ccdCase.getInterestDateType() != null
            ? InterestDate.InterestDateType.valueOf(ccdCase.getInterestDateType().name())
            : null;

        return new InterestDate(
            interestDateType,
            ccdCase.getInterestClaimStartDate(),
            ccdCase.getInterestStartDateReason(),
            endDateType
        );
    }
}
