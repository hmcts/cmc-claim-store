package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDHousingDisrepair;
import uk.gov.hmcts.cmc.domain.models.particulars.HousingDisrepair;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.MORE_THAN_THOUSAND_POUNDS;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.THOUSAND_POUNDS_OR_LESS;

public class HousingDisrepairMapperTest {

    private HousingDisrepairMapper mapper = new HousingDisrepairMapper();

    @Test
    public void shouldMapHousingDisrepairToCCD() {
        //given
        HousingDisrepair housingDisrepair = new HousingDisrepair(MORE_THAN_THOUSAND_POUNDS, THOUSAND_POUNDS_OR_LESS);

        //when
        CCDHousingDisrepair ccdHousingDisrepair = mapper.to(housingDisrepair);

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
        HousingDisrepair housingDisrepair = mapper.from(ccdHousingDisrepair);

        //then
        assertThat(ccdHousingDisrepair.getCostOfRepairsDamages())
            .isEqualTo(housingDisrepair.getCostOfRepairsDamages().name());

        assertThat(ccdHousingDisrepair.getOtherDamages())
            .isEqualTo(housingDisrepair.getOtherDamages().orElseThrow(IllegalStateException::new).name());
    }
}
