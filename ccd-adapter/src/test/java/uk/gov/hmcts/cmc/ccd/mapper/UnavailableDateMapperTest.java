package uk.gov.hmcts.cmc.ccd.mapper;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.UnavailableDate;

import java.time.LocalDate;

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
            .unavailableDate(LocalDate.of(2040, 1, 1))
            .build();

        //when
        CCDCollectionElement<LocalDate> ccdUnavailableDate = mapper.to(unavailableDate);

        //then
        Assertions.assertThat(unavailableDate.getUnavailableDate()).isEqualTo(ccdUnavailableDate.getValue());
    }

    @Test
    public void shouldMapUnavailableDateMapperFromCCD() {
        //given
        CCDCollectionElement<LocalDate> ccdUnavailableDate = CCDCollectionElement.<LocalDate>builder()
            .value(LocalDate.of(2040, 1, 1))
            .build();

        //when
        UnavailableDate unavailableDate = mapper.from(ccdUnavailableDate);

        //then
        Assertions.assertThat(ccdUnavailableDate.getValue()).isEqualTo(unavailableDate.getUnavailableDate());
    }
}
