package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDTelephone;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDRespondentCompany;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDRespondentIndividual;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDRespondentOrganisation;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDRespondentSoleTrader;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@ExtendWith(SpringExtension.class)
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
        assertEquals(
            individualParty.getPhone().orElse(null),
            individualBuilder.build().getClaimantProvidedDetail().getTelephoneNumber().getTelephoneNumber(),
            "Phone number should be mapped properly for individual party type");
        assertEquals(soleTraderParty.getPhone().orElse(null),
            soleTraderBuilder.build().getClaimantProvidedDetail().getTelephoneNumber().getTelephoneNumber(),
            "Phone number should be mapped properly for sole trader party type");
        assertEquals(companyParty.getPhone().orElse(null),
            companyBuilder.build().getClaimantProvidedDetail().getTelephoneNumber().getTelephoneNumber(),
            "Phone number should be mapped properly for company party type");
        assertEquals(
            organisationParty.getPhone().orElse(null),
            organisationBuilder.build().getClaimantProvidedDetail().getTelephoneNumber().getTelephoneNumber(),
            "Phone number should be mapped properly for organisation party type");
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
        assertEquals(SampleCCDTelephone.withDefaultPhoneNumber().getTelephoneNumber(),
            individualParty.getPhone().orElse(null),
            "Telephone no from claimant provided details not properly mapped for individual");

        TheirDetails companyParty = theirDetailsMapper
            .from(CCDCollectionElement.<CCDRespondent>builder()
                .id(collectionId)
                .value(getCCDRespondentCompany()).build());
        assertEquals(SampleCCDTelephone.withDefaultPhoneNumber().getTelephoneNumber(),
            companyParty.getPhone().orElse(null),
            "Telephone no from claimant provided details not properly mapped for company");

        TheirDetails soleTraderParty = theirDetailsMapper
            .from(CCDCollectionElement.<CCDRespondent>builder()
                .id(collectionId)
                .value(getCCDRespondentSoleTrader()).build());
        assertEquals(SampleCCDTelephone.withDefaultPhoneNumber().getTelephoneNumber(),
            soleTraderParty.getPhone().orElse(null),
            "Telephone no from claimant provided details not properly mapped for sole trader");

        TheirDetails organisationParty = theirDetailsMapper
            .from(CCDCollectionElement.<CCDRespondent>builder()
                .id(collectionId)
                .value(getCCDRespondentOrganisation()).build());
        assertEquals(
            SampleCCDTelephone.withDefaultPhoneNumber().getTelephoneNumber(),
            organisationParty.getPhone().orElse(null),
            "Telephone no from claimant provided details not properly mapped for organisation");

    }

}
