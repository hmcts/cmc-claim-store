package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.util.SampleCCDDefendant;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DefendantMapperTest {

    @Autowired
    private DefendantMapper mapper;

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenTheirDetailsIsNull() {
        mapper.to(null, SampleClaim.getDefault());
    }

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenClaimIsNull() {
        TheirDetails theirDetails = SampleTheirDetails.builder().organisationDetails();
        mapper.to(theirDetails, null);
    }

    @Test
    public void mapToCCDDefendantWithoutResponseDetails() {

        // Given
        TheirDetails theirDetails = SampleTheirDetails.builder().organisationDetails();
        Claim claim = SampleClaim.claim(SampleClaimData.submittedByClaimantBuilder().build(),
            "referenceNumber");

        //When
        CCDDefendant defendant = mapper.to(theirDetails, claim);

        //Then
        assertEquals("Claim response deadline is not proerly mapped properly",
            defendant.getResponseDeadline(), claim.getResponseDeadline());

        assertEquals("Claim response letter holder id is not mapped properly",
            defendant.getLetterHolderId(), claim.getLetterHolderId());

        assertEquals("Claim defendantId is not mapped properly",
            defendant.getDefendantId(), claim.getDefendantId());

        assertEquals("Claim defendant email is not mapped properly",
            defendant.getPartyEmail(), claim.getDefendantEmail());

        assertEquals("Claim response more time requested is not mapped properly",
            defendant.getResponseMoreTimeNeededOption().toBoolean(), claim.isMoreTimeRequested());

        assertEquals("The claimantprovidedtype should be of organization",
            defendant.getClaimantProvidedType(), CCDPartyType.ORGANISATION);

    }

    @Test
    public void mapToCCDDefendantWithResponseDetails() {
        // Given
        TheirDetails theirDetails = SampleTheirDetails.builder().individualDetails();
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation();

        //When
        CCDDefendant defendant = mapper.to(theirDetails, claim);

        //Then
        assertEquals("Claim response deadline is not proerly mapped properly",
            defendant.getResponseDeadline(), claim.getResponseDeadline());

        assertEquals("Claim response letter holder id is not mapped properly",
            defendant.getLetterHolderId(), claim.getLetterHolderId());

        assertEquals("Claim defendantId is not mapped properly",
            defendant.getDefendantId(), claim.getDefendantId());

        assertEquals("Claim defendant email is not mapped properly",
            defendant.getPartyEmail(), claim.getDefendantEmail());

        assertEquals("Claim response more time requested is not mapped properly",
            defendant.getResponseMoreTimeNeededOption().toBoolean(), claim.isMoreTimeRequested());

        //Verify if the theirdetails mapper and response mapper are called by assert not null
        assertThat(defendant.getResponseSubmittedOn(), is(notNullValue()));
        assertThat(defendant.getResponseType(), is(notNullValue()));
        assertThat(defendant.getClaimantProvidedType(), is(notNullValue()));

        assertEquals("The mapping for theirDetailsMapper is not done properly",
            defendant.getClaimantProvidedType(), CCDPartyType.INDIVIDUAL);

        assertEquals("The claim response submitted is not mapped properly when response is present",
            defendant.getResponseSubmittedOn(), claim.getRespondedAt());

        assertEquals("The Response mapper is not called / mapped when response is available",
            defendant.getResponseType().name(), claim.getResponse().get().getResponseType().name());
    }

    @Test
    public void mapTheirDetailsFromCCDClaimWithNoResponse() {
        //Given
        CCDDefendant ccdDefendant = SampleCCDDefendant.withResponseMoreTimeNeededOption();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        //when
        mapper.from(claimBuilder, ccdDefendant);
        Claim finalClaim = claimBuilder.build();

        // Then
        assertEquals("response deadline is not proerly mapped properly",
            finalClaim.getResponseDeadline(), ccdDefendant.getResponseDeadline());

        assertEquals("Claim response letter holder id is not mapped properly",
            finalClaim.getLetterHolderId(), ccdDefendant.getLetterHolderId());

        assertEquals("Claim defendantId is not mapped properly",
            finalClaim.getDefendantId(), ccdDefendant.getDefendantId());

        assertEquals("Claim defendant email is not mapped properly",
            finalClaim.getDefendantEmail(), ccdDefendant.getPartyEmail());

        assertEquals("Claim response more time requested is not mapped properly",
            finalClaim.isMoreTimeRequested(), ccdDefendant.getResponseMoreTimeNeededOption().toBoolean());

    }

    @Test
    public void mapTheirDetailsFromCCDClaimWithNoResponseMoreTimeNeededOption() {
        //Given
        CCDDefendant ccdDefendant = SampleCCDDefendant.withDefault().build();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        //when
        mapper.from(claimBuilder, ccdDefendant);
        Claim finalClaim = claimBuilder.build();

        // Then
        assertThat("Claim response more time requested is not mapped properly",
            finalClaim.isMoreTimeRequested(), is(false));

    }

    @Test
    public void mapTheirDetailsFromCCDClaimWithResponse() {
        //Given
        CCDDefendant ccdDefendant = SampleCCDDefendant.withResponse();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        //when
        TheirDetails party = mapper.from(claimBuilder, ccdDefendant);
        Claim finalClaim = claimBuilder.build();

        // Then
        assertEquals("response deadline is not proerly mapped properly",
            finalClaim.getResponseDeadline(), ccdDefendant.getResponseDeadline());

        assertEquals("Claim response letter holder id is not mapped properly",
            finalClaim.getLetterHolderId(), ccdDefendant.getLetterHolderId());

        assertEquals("Claim defendantId is not mapped properly",
            finalClaim.getDefendantId(), ccdDefendant.getDefendantId());

        assertEquals("Claim defendant email is not mapped properly",
            finalClaim.getDefendantEmail(), ccdDefendant.getPartyEmail());

        assertEquals("Claim response more time requested is not mapped properly",
            finalClaim.isMoreTimeRequested(), ccdDefendant.getResponseMoreTimeNeededOption().toBoolean());

        assertThat(party, instanceOf(IndividualDetails.class));

    }


}
