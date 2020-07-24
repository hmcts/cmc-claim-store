package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDTelephone;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDRespondentCompany;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDRespondentIndividual;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDRespondentOrganisation;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDRespondentSoleTrader;

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
    public void shouldMapClaimantProvidedDefendantPhoneToCCD() {
        //given
        String claimantProvidedPhone = "0773646636464";
        TheirDetails individualParty = SampleTheirDetails.builder().withPhone(claimantProvidedPhone)
            .individualDetails();
        TheirDetails soleTraderParty = SampleTheirDetails.builder().withPhone(claimantProvidedPhone)
            .soleTraderDetails();
        TheirDetails organisationParty = SampleTheirDetails.builder().withPhone(claimantProvidedPhone)
            .organisationDetails();
        TheirDetails companyParty = SampleTheirDetails.builder().withPhone(claimantProvidedPhone)
            .companyDetails();

        //when
        CCDRespondent.CCDRespondentBuilder individualBuilder = CCDRespondent.builder();
        CCDRespondent.CCDRespondentBuilder soleTraderBuilder = CCDRespondent.builder();
        CCDRespondent.CCDRespondentBuilder companyBuilder = CCDRespondent.builder();
        CCDRespondent.CCDRespondentBuilder organisationBuilder = CCDRespondent.builder();

        theirDetailsMapper.to(individualBuilder, individualParty);
        theirDetailsMapper.to(soleTraderBuilder, soleTraderParty);
        theirDetailsMapper.to(companyBuilder, companyParty);
        theirDetailsMapper.to(organisationBuilder, organisationParty);

        //then
        assertEquals("Phone number should be mapped properly for individual party type",
            individualParty.getPhone().orElse(null),
            individualBuilder.build().getClaimantProvidedDetail().getTelephoneNumber().getTelephoneNumber());
        assertEquals("Phone number should be mapped properly for sole trader party type",
            soleTraderParty.getPhone().orElse(null),
            soleTraderBuilder.build().getClaimantProvidedDetail().getTelephoneNumber().getTelephoneNumber());
        assertEquals("Phone number should be mapped properly for company party type",
            companyParty.getPhone().orElse(null),
            companyBuilder.build().getClaimantProvidedDetail().getTelephoneNumber().getTelephoneNumber());
        assertEquals("Phone number should be mapped properly for organisation party type",
            organisationParty.getPhone().orElse(null),
            organisationBuilder.build().getClaimantProvidedDetail().getTelephoneNumber().getTelephoneNumber());
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
        String claimantProvidedEmail = ccdParty.getPartyDetail().getEmailAddress();
        CCDParty claimantProvidedDetails = ccdParty.getClaimantProvidedDetail().toBuilder()
            .emailAddress(claimantProvidedEmail).build();
        CCDRespondent ccdPartyWithEmail = ccdParty.toBuilder().claimantProvidedDetail(claimantProvidedDetails).build();

        assertThat(party).isEqualTo(ccdPartyWithEmail);
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

    @Test
    public void shouldMapClaimantProvidedDefendantPhoneFromCCD() {
        String collectionId = UUID.randomUUID().toString();

        TheirDetails individualParty = theirDetailsMapper
            .from(CCDCollectionElement.<CCDRespondent>builder()
                .id(collectionId)
                .value(getCCDRespondentIndividual()).build());
        assertEquals("Telephone no from claimant provided details not properly mapped for individual",
            SampleCCDTelephone.withDefaultPhoneNumber().getTelephoneNumber(),
            individualParty.getPhone().orElse(null));

        TheirDetails companyParty = theirDetailsMapper
            .from(CCDCollectionElement.<CCDRespondent>builder()
                .id(collectionId)
                .value(getCCDRespondentCompany()).build());
        assertEquals("Telephone no from claimant provided details not properly mapped for company",
            SampleCCDTelephone.withDefaultPhoneNumber().getTelephoneNumber(),
            companyParty.getPhone().orElse(null));

        TheirDetails soleTraderParty = theirDetailsMapper
            .from(CCDCollectionElement.<CCDRespondent>builder()
                .id(collectionId)
                .value(getCCDRespondentSoleTrader()).build());
        assertEquals("Telephone no from claimant provided details not properly mapped for sole trader",
            SampleCCDTelephone.withDefaultPhoneNumber().getTelephoneNumber(),
            soleTraderParty.getPhone().orElse(null));

        TheirDetails organisationParty = theirDetailsMapper
            .from(CCDCollectionElement.<CCDRespondent>builder()
                .id(collectionId)
                .value(getCCDRespondentOrganisation()).build());
        assertEquals("Telephone no from claimant provided details not properly mapped for organisation",
            SampleCCDTelephone.withDefaultPhoneNumber().getTelephoneNumber(),
            organisationParty.getPhone().orElse(null));

    }

}
