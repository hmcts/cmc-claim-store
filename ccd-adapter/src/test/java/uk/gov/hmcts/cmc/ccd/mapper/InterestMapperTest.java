package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestEndDateType;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.cmc.domain.models.InterestDate.InterestEndDateType.SUBMISSION;

public class InterestMapperTest {

    private final InterestMapper interestMapper = new InterestMapper(
        new InterestBreakdownMapper(new MoneyMapper()),
        new InterestDateMapper(),
        new MoneyMapper()
    );

    @Test
    public void shouldMapToCase() {

        CCDCase.CCDCaseBuilder builder = CCDCase.builder();
        String explanation = "explanation";
        Interest interest = SampleInterest.builder()
            .withInterestBreakdown(new InterestBreakdown(BigDecimal.valueOf(72.27), explanation))
            .withRate(null)
            .withType(null)
            .withReason(null)
            .withSpecificDailyAmount(null)
            .withInterestDate(new InterestDate(null, null, null, SUBMISSION))
            .build();

        interestMapper.to(interest, builder);

        CCDCase ccdCase = builder.build();
        assertEquals(ccdCase.getInterestEndDateType().name(), SUBMISSION.name());
        assertEquals(ccdCase.getInterestBreakDownAmount(), "7227");
        assertEquals(ccdCase.getInterestBreakDownExplanation(), explanation);
        assertNull(ccdCase.getInterestType());
        assertNull(ccdCase.getInterestRate());
        assertNull(ccdCase.getInterestReason());
        assertNull(ccdCase.getInterestType());
    }

    @Test
    public void shouldMapFromCase() {
        String explanation = "explanation";
        CCDCase ccdCase = CCDCase.builder()
            .interestEndDateType(CCDInterestEndDateType.SUBMISSION)
            .interestBreakDownAmount("7227")
            .interestBreakDownExplanation(explanation)
            .build();

        Interest interest = interestMapper.from(ccdCase);
        assertEquals(interest.getInterestDate().getEndDateType().name(), SUBMISSION.name());
        assertEquals(interest.getInterestBreakdown().getTotalAmount(), BigDecimal.valueOf(72.27));
        assertEquals(ccdCase.getInterestBreakDownExplanation(), explanation);
        assertNull(ccdCase.getInterestType());
        assertNull(ccdCase.getInterestRate());
        assertNull(ccdCase.getInterestReason());
        assertNull(ccdCase.getInterestType());
    }
}
