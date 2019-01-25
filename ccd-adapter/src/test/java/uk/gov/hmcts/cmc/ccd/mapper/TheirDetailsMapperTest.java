package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDDefendantCompany;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDDefendantIndividual;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDDefendantOrganisation;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDDefendantSoleTrader;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TheirDetailsMapperTest {
    @Autowired
    private TheirDetailsMapper theirDetailsMapper;

    @Test
    public void shouldMapIndividualToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().individualDetails();

        //when
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        theirDetailsMapper.to(builder, party);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapCompanyToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().companyDetails();

        //when
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        theirDetailsMapper.to(builder, party);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapOrganisationToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().organisationDetails();

        //when
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        theirDetailsMapper.to(builder, party);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapSoleTraderToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().soleTraderDetails();

        //when
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        theirDetailsMapper.to(builder, party);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapIndividualFromCCD() {
        //given
        CCDDefendant ccdParty = getCCDDefendantIndividual();
        String collectionId = UUID.randomUUID().toString();

        //when
        TheirDetails party = theirDetailsMapper
            .from(CCDCollectionElement.<CCDDefendant>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

    @Test
    public void shouldMapCompanyFromCCD() {
        //given
        CCDDefendant ccdParty = getCCDDefendantCompany();
        String collectionId = UUID.randomUUID().toString();

        //when
        TheirDetails party = theirDetailsMapper
            .from(CCDCollectionElement.<CCDDefendant>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

    @Test
    public void shouldMapOrganisationFromCCD() {
        //given
        CCDDefendant ccdParty = getCCDDefendantOrganisation();
        String collectionId = UUID.randomUUID().toString();

        //when
        TheirDetails party = theirDetailsMapper
            .from(CCDCollectionElement.<CCDDefendant>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

    @Test
    public void shouldMapSoleTraderFromCCD() {
        //given
        CCDDefendant ccdParty = getCCDDefendantSoleTrader();
        String collectionId = UUID.randomUUID().toString();

        //when
        TheirDetails party = theirDetailsMapper
            .from(CCDCollectionElement.<CCDDefendant>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

}
