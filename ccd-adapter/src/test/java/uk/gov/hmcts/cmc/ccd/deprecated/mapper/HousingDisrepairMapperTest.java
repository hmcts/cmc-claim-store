package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDHousingDisrepair;
import uk.gov.hmcts.cmc.domain.models.particulars.HousingDisrepair;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.MORE_THAN_THOUSAND_POUNDS;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.THOUSAND_POUNDS_OR_LESS;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class HousingDisrepairMapperTest {

    @Autowired
    private HousingDisrepairMapper housingDisrepairMapper;

    @Test
    public void shouldMapHousingDisrepairToCCD() {
        //given
        HousingDisrepair housingDisrepair = new HousingDisrepair(MORE_THAN_THOUSAND_POUNDS, THOUSAND_POUNDS_OR_LESS);

        //when
        CCDHousingDisrepair ccdHousingDisrepair = housingDisrepairMapper.to(housingDisrepair);

        //then
        assertThat(ccdHousingDisrepair.getCostOfRepairsDamages())
            .isEqualTo(housingDisrepair.getCostOfRepairsDamages().name());

        assertThat(ccdHousingDisrepair.getOtherDamages())
            .isEqualTo(housingDisrepair.getOtherDamages().orElseThrow(IllegalStateException::new).name());
    }

    @Test
    public void shouldMapHousingDisrepairFromCCD() {
        //given
        CCDHousingDisrepair ccdHousingDisrepair = CCDHousingDisrepair.builder()
            .otherDamages(MORE_THAN_THOUSAND_POUNDS.name())
            .costOfRepairsDamages(THOUSAND_POUNDS_OR_LESS.name())
            .build();

        //when
        HousingDisrepair housingDisrepair = housingDisrepairMapper.from(ccdHousingDisrepair);

        //then
        assertThat(ccdHousingDisrepair.getCostOfRepairsDamages())
            .isEqualTo(housingDisrepair.getCostOfRepairsDamages().name());

        assertThat(ccdHousingDisrepair.getOtherDamages())
            .isEqualTo(housingDisrepair.getOtherDamages().orElseThrow(IllegalStateException::new).name());
    }
}
