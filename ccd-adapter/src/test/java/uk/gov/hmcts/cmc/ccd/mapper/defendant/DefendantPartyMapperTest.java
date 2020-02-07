package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.DefendantPartyAssert;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import java.util.UUID;

import static uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDDefendant.withPartyCompany;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDDefendant.withPartyIndividual;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDDefendant.withPartyOrganisation;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDDefendant.withPartySoleTrader;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DefendantPartyMapperTest {

    @Autowired
    private DefendantPartyMapper mapper;

    private DefendantPartyAssert assertThat(Party party) {
        return new DefendantPartyAssert(party);
    }

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenBuilderIsNull() {
        //noinspection ConstantConditions
        mapper.to(null, SampleParty.builder().individual(), null);
    }

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenPartyIsNull() {
        //noinspection ConstantConditions
        mapper.to(CCDRespondent.builder(), null, null);
    }

    @Test
    public void shouldMapIndividualToCCD() {
        //given
        Party party = SampleParty.builder().individual();
        CCDParty.CCDPartyBuilder partyBuilder = CCDParty.builder();

        //when
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        mapper.to(builder, party, partyBuilder);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapCompanyToCCD() {
        //given
        Party party = SampleParty.builder().company();
        CCDParty.CCDPartyBuilder partyBuilder = CCDParty.builder();

        //when
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        mapper.to(builder, party, partyBuilder);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapOrganisationToCCD() {
        //given
        Party party = SampleParty.builder().organisation();
        CCDParty.CCDPartyBuilder partyBuilder = CCDParty.builder();

        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        mapper.to(builder, party, partyBuilder);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapSoleTraderToCCD() {
        //given
        Party party = SampleParty.builder().soleTrader();
        CCDParty.CCDPartyBuilder partyBuilder = CCDParty.builder();

        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        mapper.to(builder, party, partyBuilder);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapIndividualFromCCD() {
        //given
        CCDRespondent ccdRespondent = withPartyIndividual()
            .claimantProvidedDetail(CCDParty.builder()
                .title("Mrs.")
                .firstName("Mary")
                .lastName("Richards")
                .build())
            .build();

        CCDCollectionElement<CCDRespondent> respondentElement = CCDCollectionElement.<CCDRespondent>builder()
            .value(ccdRespondent)
            .id(UUID.randomUUID().toString())
            .build();
        //when
        Party party = mapper.from(respondentElement);

        //then
        assertThat(party).isEqualTo(ccdRespondent);
    }

    @Test
    public void shouldMapCompanyFromCCD() {
        //given
        CCDRespondent ccdRespondent = withPartyCompany().build();

        CCDCollectionElement<CCDRespondent> respondentElement = CCDCollectionElement.<CCDRespondent>builder()
            .value(ccdRespondent)
            .id(UUID.randomUUID().toString())
            .build();

        //when
        Party party = mapper.from(respondentElement);

        //then
        assertThat(party).isEqualTo(ccdRespondent);
    }

    @Test
    public void shouldMapSoleTraderFromCCD() {
        //given
        CCDRespondent ccdRespondent = withPartySoleTrader()
            .claimantProvidedDetail(CCDParty.builder()
                .title("Mrs.")
                .firstName("Mary")
                .lastName("Richards")
                .build()).build();

        CCDCollectionElement<CCDRespondent> respondentElement = CCDCollectionElement.<CCDRespondent>builder()
            .value(ccdRespondent)
            .id(UUID.randomUUID().toString())
            .build();

        //when
        Party party = mapper.from(respondentElement);

        //then
        assertThat(party).isEqualTo(ccdRespondent);
    }

    @Test
    public void shouldMapOrganisationFromCCD() {
        //given
        CCDRespondent ccdRespondent = withPartyOrganisation().build();

        CCDCollectionElement<CCDRespondent> respondentElement = CCDCollectionElement.<CCDRespondent>builder()
            .value(ccdRespondent)
            .id(UUID.randomUUID().toString())
            .build();

        //when
        Party party = mapper.from(respondentElement);

        //then
        assertThat(party).isEqualTo(ccdRespondent);
    }
}
