package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDInterestDate;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDInterestDateType;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.InterestDate.InterestDateType.SUBMISSION;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class InterestDateMapperTest {

    @Autowired
    private InterestDateMapper interestDateMapper;

    @Test
    public void shouldMapInterestDateToCCD() {
        //given
        final InterestDate interestDate = SampleInterestDate.builder().build();

        //when
        CCDInterestDate ccdInterestDate = interestDateMapper.to(interestDate);

        //then
        assertThat(interestDate).isEqualTo(ccdInterestDate);
    }

    @Test
    public void shouldMapSubmissionInterestDateToCCD() {
        //given
        final InterestDate interestDate = SampleInterestDate.builder().withType(SUBMISSION).build();

        //when
        CCDInterestDate ccdInterestDate = interestDateMapper.to(interestDate);

        //then
        assertThat(interestDate).isEqualTo(ccdInterestDate);
    }

    @Test
    public void shouldMapCustomInterestDateFromCCD() {
        //given
        CCDInterestDate ccdInterestDate = CCDInterestDate.builder()
            .date(LocalDate.now())
            .reason("reason")
            .type(CCDInterestDateType.CUSTOM)
            .build();

        //when
        InterestDate interestDate = interestDateMapper.from(ccdInterestDate);

        //then
        assertThat(interestDate).isEqualTo(ccdInterestDate);
    }

    @Test
    public void shouldMapSubmissionInterestDateFromCCD() {
        //given
        CCDInterestDate ccdInterestDate = CCDInterestDate.builder()
            .date(LocalDate.now())
            .reason("reason")
            .type(CCDInterestDateType.SUBMISSION)
            .build();

        //when
        InterestDate interestDate = interestDateMapper.from(ccdInterestDate);

        //then
        assertThat(interestDate).isEqualTo(ccdInterestDate);
    }
}
