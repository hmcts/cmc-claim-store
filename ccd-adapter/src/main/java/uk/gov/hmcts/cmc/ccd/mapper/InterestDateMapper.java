package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDate;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDateType;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

@Component
public class InterestDateMapper implements Mapper<CCDInterestDate, InterestDate> {

    @Override
    public CCDInterestDate to(InterestDate interestDate) {
        CCDInterestDate.CCDInterestDateBuilder builder = CCDInterestDate.builder();
        return builder.type(CCDInterestDateType.valueOf(interestDate.getType().name()))
            .date(interestDate.getDate())
            .reason(interestDate.getReason())
            .build();
    }

    @Override
    public InterestDate from(CCDInterestDate ccdInterestDate) {
        return new InterestDate(
            InterestDate.InterestDateType.valueOf(ccdInterestDate.getType().name()),
            ccdInterestDate.getDate(),
            ccdInterestDate.getReason()
        );
    }
}
