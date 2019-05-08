package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestType;
import uk.gov.hmcts.cmc.domain.models.Interest;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class InterestMapper implements BuilderMapper<CCDCase, Interest, CCDCase.CCDCaseBuilder> {

    private final InterestBreakdownMapper interestBreakdownMapper;
    private final InterestDateMapper interestDateMapper;
    private final MoneyMapper moneyMapper;

    @Autowired
    public InterestMapper(
        InterestBreakdownMapper interestBreakdownMapper,
        InterestDateMapper interestDateMapper,
        MoneyMapper moneyMapper
    ) {
        this.interestBreakdownMapper = interestBreakdownMapper;
        this.interestDateMapper = interestDateMapper;
        this.moneyMapper = moneyMapper;
    }

    @Override
    public void to(Interest interest, CCDCase.CCDCaseBuilder builder) {
        if (interest == null) {
            return;
        }

        builder.interestSpecificDailyAmount(interest.getSpecificDailyAmount().map(moneyMapper::to).orElse(null));
        interestBreakdownMapper.to(interest.getInterestBreakdown(), builder);
        interestDateMapper.to(interest.getInterestDate(), builder);

        builder
            .interestType(CCDInterestType.valueOf(interest.getType().name()))
            .interestRate(interest.getRate())
            .interestReason(interest.getReason());
    }

    @Override
    public Interest from(CCDCase ccdCase) {
        if (ccdCase.getInterestType() == null
            && ccdCase.getInterestRate() == null
            && isBlank(ccdCase.getInterestReason())
            && ccdCase.getInterestSpecificDailyAmount() == null
            && ccdCase.getInterestBreakDownAmount() == null
            && isBlank(ccdCase.getInterestBreakDownExplanation())
            && ccdCase.getInterestDateType() == null
        ) {
            return null;
        }

        return new Interest(
            Interest.InterestType.valueOf(ccdCase.getInterestType().name()),
            interestBreakdownMapper.from(ccdCase),
            ccdCase.getInterestRate(),
            ccdCase.getInterestReason(),
            moneyMapper.from(ccdCase.getInterestSpecificDailyAmount()),
            interestDateMapper.from(ccdCase)
        );
    }
}
