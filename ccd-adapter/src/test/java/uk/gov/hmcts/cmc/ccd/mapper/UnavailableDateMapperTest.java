package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDUnavailableDate;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.UnavailableDate;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class UnavailableDateMapperTest {
    @Autowired
    private UnavailableDateMapper mapper;

    @Test
    public void shouldMapUnavailableDateMapperToCCD() {
        //given
        UnavailableDate unavailableDate = UnavailableDate
            .builder()
            .unavailableDate(LocalDate.of(2040,1,1))
            .build();

        //when
        CCDCollectionElement<CCDUnavailableDate> ccdUnavailableDate = mapper.to(unavailableDate);

        //then
        assertThat(unavailableDate.getUnavailableDate(), is(ccdUnavailableDate.getValue().getUnavailableDate()));
    }

    @Test
    public void shouldMapUnavailableDateMapperFromCCD() {
        //given
        CCDUnavailableDate ccdUnavailableDate = CCDUnavailableDate
            .builder()
            .unavailableDate(LocalDate.of(2041,1,1))
            .build();

        //when
        UnavailableDate unavailableDate = mapper.from(CCDCollectionElement.<CCDUnavailableDate>builder()
            .value(ccdUnavailableDate).build());

        //then
        assertThat(ccdUnavailableDate.getUnavailableDate(), is(unavailableDate.getUnavailableDate()));
    }
}
