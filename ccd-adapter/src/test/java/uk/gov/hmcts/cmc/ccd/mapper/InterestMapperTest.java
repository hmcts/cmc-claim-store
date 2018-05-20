package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterest;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDate;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDateType;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestEndDateType;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestType;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.BREAKDOWN;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.NO_INTEREST;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.STANDARD;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class InterestMapperTest {

    @Autowired
    private InterestMapper interestMapper;

    @Test
    public void shouldMapInterestToCCD() {
        //given
        final Interest interest = SampleInterest.builder().build();

        //when
        CCDInterest ccdInterest = interestMapper.to(interest);

        //then
        assertThat(interest).isEqualTo(ccdInterest);
    }

    @Test
    public void shouldMapStandardInterestToCCD() {
        //given
        final Interest interest = SampleInterest.builder().withType(STANDARD).build();

        //when
        CCDInterest ccdInterest = interestMapper.to(interest);

        //then
        assertThat(interest).isEqualTo(ccdInterest);
    }

    @Test
    public void shouldMapStandardInterestWithInterestDateToCCD() {
        //given
        final Interest interest = SampleInterest.builder()
            .withType(STANDARD)
            .withInterestDate(SampleInterestDate.validDefaults())
            .build();

        //when
        CCDInterest ccdInterest = interestMapper.to(interest);

        //then
        assertThat(interest).isEqualTo(ccdInterest);
    }

    @Test
    public void shouldMapNoInterestToCCD() {
        //given
        final Interest interest = SampleInterest.builder().withType(NO_INTEREST).build();

        //when
        CCDInterest ccdInterest = interestMapper.to(interest);

        //then
        assertThat(interest).isEqualTo(ccdInterest);
    }

    @Test
    public void shouldMapBreakdownInterestToCCD() {
        //given
        final Interest interest = SampleInterest.builder().withType(BREAKDOWN)
            .withInterestBreakdown(SampleInterestBreakdown.builder().build())
            .withSpecificDailyAmount(new BigDecimal(5))
            .withInterestDate(SampleInterestDate.submissionToSettledOrJudgement())
            .build();

        //when
        CCDInterest ccdInterest = interestMapper.to(interest);

        //then
        assertThat(interest).isEqualTo(ccdInterest);
    }

    @Test
    public void shouldMapStandardInterestFromCCD() {
        //given
        CCDInterest ccdInterest = CCDInterest.builder()
            .rate(BigDecimal.valueOf(40))
            .reason("reason")
            .type(CCDInterestType.STANDARD)
            .build();

        //when
        Interest interest = interestMapper.from(ccdInterest);

        //then
        assertThat(interest).isEqualTo(ccdInterest);
    }

    @Test
    public void shouldMapStandardInterestWithInterestDateFromCCD() {
        CCDInterestDate interestDate = CCDInterestDate.builder()
            .type(CCDInterestDateType.CUSTOM)
            .date(LocalDate.now())
            .reason("A reason")
            .endDateType(CCDInterestEndDateType.SETTLED_OR_JUDGMENT)
            .build();

        //given
        CCDInterest ccdInterest = CCDInterest.builder()
            .rate(BigDecimal.valueOf(40))
            .reason("reason")
            .type(CCDInterestType.STANDARD)
            .interestDate(interestDate)
            .build();

        //when
        Interest interest = interestMapper.from(ccdInterest);

        //then
        assertThat(interest).isEqualTo(ccdInterest);
    }

    @Test
    public void shouldMapDifferentInterestFromCCD() {
        //given
        CCDInterest ccdInterest = CCDInterest.builder()
            .rate(BigDecimal.valueOf(40))
            .reason("reason")
            .type(CCDInterestType.DIFFERENT)
            .build();

        //when
        Interest interest = interestMapper.from(ccdInterest);

        //then
        assertThat(interest).isEqualTo(ccdInterest);
    }

    @Test
    public void shouldMapNoInterestFromCCD() {
        //given
        CCDInterest ccdInterest = CCDInterest.builder()
            .reason("reason")
            .type(CCDInterestType.NO_INTEREST)
            .build();

        //when
        Interest interest = interestMapper.from(ccdInterest);

        //then
        assertThat(interest).isEqualTo(ccdInterest);
    }
}
