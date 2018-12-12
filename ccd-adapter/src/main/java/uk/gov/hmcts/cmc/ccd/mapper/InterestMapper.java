package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestType;
import uk.gov.hmcts.cmc.domain.models.Interest;

@Component
public class InterestMapper implements BuilderMapper<CCDCase, Interest, CCDCase.CCDCaseBuilder> {

    private InterestBreakdownMapper interestBreakdownMapper;
    private InterestDateMapper interestDateMapper;

    @Autowired
    public InterestMapper(
        InterestBreakdownMapper interestBreakdownMapper,
        InterestDateMapper interestDateMapper
    ) {
        this.interestBreakdownMapper = interestBreakdownMapper;
        this.interestDateMapper = interestDateMapper;
    }

    @Override
    public void to(Interest interest, CCDCase.CCDCaseBuilder builder) {
        if (interest == null) {
            return;
        }

        interest.getSpecificDailyAmount().ifPresent(builder::interestSpecificDailyAmount);
        interestBreakdownMapper.to(interest.getInterestBreakdown(), builder);
        interestDateMapper.to(interest.getInterestDate(), builder);

        builder
            .interestType(CCDInterestType.valueOf(interest.getType().name()))
            .interestRate(interest.getRate())
            .reason(interest.getReason());
    }

    @Override
    public Interest from(CCDCase ccdCase) {
        if (ccdCase == null) {
            return null;
        }

        return new Interest(
            Interest.InterestType.valueOf(ccdCase.getInterestType().name()),
            interestBreakdownMapper.from(ccdCase),
            ccdCase.getInterestRate(),
            ccdCase.getReason(),
            ccdCase.getInterestSpecificDailyAmount(),
            interestDateMapper.from(ccdCase)
        );
    }
}
