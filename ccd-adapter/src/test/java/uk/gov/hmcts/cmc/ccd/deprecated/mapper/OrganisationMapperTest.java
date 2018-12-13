package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDOrganisation;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.deprecated.SampleData.getCCDOrganisation;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class OrganisationMapperTest {

    @Autowired
    private OrganisationMapper organisationMapper;

    @Test
    public void shouldMapOrganisationToCCD() {
        //given
        Organisation organisation = SampleParty.builder().organisation();

        //when
        CCDOrganisation ccdOrganisation = organisationMapper.to(organisation);

        //then
        assertThat(organisation).isEqualTo(ccdOrganisation);
    }

    @Test
    public void sholdMapOrganisationFromCCD() {
        //given
        CCDOrganisation ccdOrganisation = getCCDOrganisation();

        //when
        Organisation organisation = organisationMapper.from(ccdOrganisation);

        //then
        assertThat(organisation).isEqualTo(ccdOrganisation);
    }

}
