package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterest;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestType;
import uk.gov.hmcts.cmc.domain.models.Interest;

@Component
public class InterestMapper implements Mapper<CCDInterest, Interest> {

    private InterestBreakdownMapper interestBreakdownMapper;

    @Autowired
    public InterestMapper(InterestBreakdownMapper interestBreakdownMapper) {
        this.interestBreakdownMapper = interestBreakdownMapper;
    }

    @Override
    public CCDInterest to(Interest interest) {
        if (interest == null) {
            return null;
        }

        CCDInterest.CCDInterestBuilder builder = CCDInterest.builder();
        return builder.type(CCDInterestType.valueOf(interest.getType().name()))
            .rate(interest.getRate())
            .reason(interest.getReason())
            .build();
    }

    @Override
    public Interest from(CCDInterest ccdInterest) {
        if (ccdInterest == null) {
            return null;
        }

        return new Interest(
            Interest.InterestType.valueOf(ccdInterest.getType().name()),
            interestBreakdownMapper.from(ccdInterest.getInterestBreakdown()),
            ccdInterest.getRate(),
            ccdInterest.getReason(),
            ccdInterest.getSpecificDailyAmount(),
            null
        );
    }
}
