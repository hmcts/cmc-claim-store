package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDClaimantCompany;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDClaimantIndividual;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDClaimantOrganisation;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDClaimantSoleTrader;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimantMapperTest {
    private Claim claim = SampleClaim.getDefault();

    @Autowired
    private ClaimantMapper claimantMapper;

    @Test
    public void shouldMapIndividualToCCD() {
        //given
        Party party = SampleParty.builder().individual();

        //when
        CCDCollectionElement<CCDClaimant> ccdParty = claimantMapper.to(party, claim);
        CCDClaimant ccdClaimant = ccdParty.getValue();

        //then
        assertThat(party).isEqualTo(ccdClaimant);
        assertThat(ccdClaimant.getPartyEmail()).isEqualTo(claim.getSubmitterEmail());
    }

    @Test
    public void shouldMapCompanyToCCD() {
        //given
        Party party = SampleParty.builder().company();

        //when
        CCDCollectionElement<CCDClaimant> ccdParty = claimantMapper.to(party, claim);
        CCDClaimant ccdClaimant = ccdParty.getValue();

        //then
        assertThat(party).isEqualTo(ccdClaimant);
        assertThat(ccdClaimant.getPartyEmail()).isEqualTo(claim.getSubmitterEmail());
    }

    @Test
    public void shouldMapOrganisationToCCD() {
        //given
        Party party = SampleParty.builder().organisation();

        //when
        CCDCollectionElement<CCDClaimant> ccdParty = claimantMapper.to(party, claim);
        CCDClaimant ccdClaimant = ccdParty.getValue();

        //then
        assertThat(party).isEqualTo(ccdClaimant);
        assertThat(ccdClaimant.getPartyEmail()).isEqualTo(claim.getSubmitterEmail());
    }

    @Test
    public void shouldMapSoleTraderToCCD() {
        //given
        Party party = SampleParty.builder().soleTrader();

        //when
        CCDCollectionElement<CCDClaimant> ccdParty = claimantMapper.to(party, claim);
        CCDClaimant ccdClaimant = ccdParty.getValue();

        //then
        assertThat(party).isEqualTo(ccdClaimant);
        assertThat(ccdClaimant.getPartyEmail()).isEqualTo(claim.getSubmitterEmail());
    }

    @Test
    public void shouldMapIndividualFromCCD() {
        //given
        CCDClaimant ccdParty = getCCDClaimantIndividual();
        String collectionId = UUID.randomUUID().toString();

        //when
        Party party = claimantMapper
            .from(CCDCollectionElement.<CCDClaimant>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

    @Test
    public void shouldMapCompanyFromCCD() {
        //given
        CCDClaimant ccdParty = getCCDClaimantCompany();
        String collectionId = UUID.randomUUID().toString();

        //when
        Party party = claimantMapper
            .from(CCDCollectionElement.<CCDClaimant>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

    @Test
    public void shouldMapOrganisationFromCCD() {
        //given
        CCDClaimant ccdParty = getCCDClaimantOrganisation();
        String collectionId = UUID.randomUUID().toString();

        //when
        Party party = claimantMapper
            .from(CCDCollectionElement.<CCDClaimant>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

    @Test
    public void shouldMapSoleTraderFromCCD() {
        //given
        CCDClaimant ccdParty = getCCDClaimantSoleTrader();
        String collectionId = UUID.randomUUID().toString();

        //when
        Party party = claimantMapper
            .from(CCDCollectionElement.<CCDClaimant>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

}
