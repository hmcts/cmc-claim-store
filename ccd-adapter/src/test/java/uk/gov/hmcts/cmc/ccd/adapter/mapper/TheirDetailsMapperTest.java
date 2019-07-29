package uk.gov.hmcts.cmc.ccd.adapter.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.adapter.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.adapter.util.SampleData.getCCDRespondentCompany;
import static uk.gov.hmcts.cmc.ccd.adapter.util.SampleData.getCCDRespondentIndividual;
import static uk.gov.hmcts.cmc.ccd.adapter.util.SampleData.getCCDRespondentOrganisation;
import static uk.gov.hmcts.cmc.ccd.adapter.util.SampleData.getCCDRespondentSoleTrader;

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
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        theirDetailsMapper.to(builder, party);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapCompanyToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().companyDetails();

        //when
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        theirDetailsMapper.to(builder, party);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapOrganisationToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().organisationDetails();

        //when
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        theirDetailsMapper.to(builder, party);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapSoleTraderToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().soleTraderDetails();

        //when
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        theirDetailsMapper.to(builder, party);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapIndividualFromCCD() {
        //given
        CCDRespondent ccdParty = getCCDRespondentIndividual();
        String collectionId = UUID.randomUUID().toString();

        //when
        TheirDetails party = theirDetailsMapper
            .from(CCDCollectionElement.<CCDRespondent>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

    @Test
    public void shouldMapCompanyFromCCD() {
        //given
        CCDRespondent ccdParty = getCCDRespondentCompany();
        String collectionId = UUID.randomUUID().toString();

        //when
        TheirDetails party = theirDetailsMapper
            .from(CCDCollectionElement.<CCDRespondent>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

    @Test
    public void shouldMapOrganisationFromCCD() {
        //given
        CCDRespondent ccdParty = getCCDRespondentOrganisation();
        String collectionId = UUID.randomUUID().toString();

        //when
        TheirDetails party = theirDetailsMapper
            .from(CCDCollectionElement.<CCDRespondent>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

    @Test
    public void shouldMapSoleTraderFromCCD() {
        //given
        CCDRespondent ccdParty = getCCDRespondentSoleTrader();
        String collectionId = UUID.randomUUID().toString();

        //when
        TheirDetails party = theirDetailsMapper
            .from(CCDCollectionElement.<CCDRespondent>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

}
