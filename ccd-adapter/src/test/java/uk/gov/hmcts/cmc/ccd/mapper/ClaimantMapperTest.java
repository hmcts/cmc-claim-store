package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDApplicantCompany;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDApplicantIndividual;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDApplicantOrganisation;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDApplicantSoleTrader;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimantMapperTest {
    private final Claim claim = SampleClaim.getDefault();

    @Autowired
    private ClaimantMapper claimantMapper;

    @Test
    public void shouldMapIndividualToCCD() {
        //given
        Party party = SampleParty.builder().individual();

        //when
        CCDCollectionElement<CCDApplicant> ccdParty = claimantMapper.to(party, claim, true);
        CCDApplicant applicant = ccdParty.getValue();

        //then
        assertThat(applicant.getLeadApplicantIndicator()).isEqualTo(YES);
        assertThat(party).isEqualTo(applicant);
        assertThat(applicant.getPartyDetail().getEmailAddress()).isEqualTo(claim.getSubmitterEmail());
    }

    @Test
    public void shouldMapCompanyToCCD() {
        //given
        Party party = SampleParty.builder().company();

        //when
        CCDCollectionElement<CCDApplicant> ccdParty = claimantMapper.to(party, claim, false);
        CCDApplicant applicant = ccdParty.getValue();

        //then
        assertThat(applicant.getLeadApplicantIndicator()).isEqualTo(NO);
        assertThat(party).isEqualTo(applicant);
        assertThat(applicant.getPartyDetail().getEmailAddress()).isEqualTo(claim.getSubmitterEmail());
    }

    @Test
    public void shouldMapOrganisationToCCD() {
        //given
        Party party = SampleParty.builder().organisation();

        //when
        CCDCollectionElement<CCDApplicant> ccdParty = claimantMapper.to(party, claim, true);
        CCDApplicant applicant = ccdParty.getValue();

        //then
        assertThat(applicant.getLeadApplicantIndicator()).isEqualTo(YES);
        assertThat(party).isEqualTo(applicant);
        assertThat(applicant.getPartyDetail().getEmailAddress()).isEqualTo(claim.getSubmitterEmail());
    }

    @Test
    public void shouldMapSoleTraderToCCD() {
        //given
        Party party = SampleParty.builder().soleTrader();

        //when
        CCDCollectionElement<CCDApplicant> ccdParty = claimantMapper.to(party, claim, true);
        CCDApplicant applicant = ccdParty.getValue();

        //then
        assertThat(applicant.getLeadApplicantIndicator()).isEqualTo(YES);
        assertThat(party).isEqualTo(applicant);
        assertThat(applicant.getPartyDetail().getEmailAddress()).isEqualTo(claim.getSubmitterEmail());
    }

    @Test
    public void shouldMapIndividualFromCCD() {
        //given
        CCDApplicant ccdParty = getCCDApplicantIndividual();
        String collectionId = UUID.randomUUID().toString();

        //when
        Party party = claimantMapper
            .from(CCDCollectionElement.<CCDApplicant>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

    @Test
    public void shouldMapCompanyFromCCD() {
        //given
        CCDApplicant ccdParty = getCCDApplicantCompany();
        String collectionId = UUID.randomUUID().toString();

        //when
        Party party = claimantMapper
            .from(CCDCollectionElement.<CCDApplicant>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

    @Test
    public void shouldMapOrganisationFromCCD() {
        //given
        CCDApplicant ccdParty = getCCDApplicantOrganisation();
        String collectionId = UUID.randomUUID().toString();

        //when
        Party party = claimantMapper
            .from(CCDCollectionElement.<CCDApplicant>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

    @Test
    public void shouldMapSoleTraderFromCCD() {
        //given
        CCDApplicant ccdParty = getCCDApplicantSoleTrader();
        String collectionId = UUID.randomUUID().toString();

        //when
        Party party = claimantMapper
            .from(CCDCollectionElement.<CCDApplicant>builder()
                .id(collectionId)
                .value(ccdParty).build());

        //then
        assertThat(party).isEqualTo(ccdParty);
        assertThat(party.getId()).isEqualTo(collectionId);
    }

}
