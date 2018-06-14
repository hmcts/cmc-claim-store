package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

@RunWith(MockitoJUnitRunner.class)
public class DefendantAddressDifferTest {

    @Test
    public void getDiffShouldReturnDiffIfAddressWasChanged() {
        Claim claim = buildClaimWithContactDetails(getAddress1(), getAddress2(), getPhoneNumber1());
    }

    private Claim buildClaimWithContactDetails(
        Address address,
        Address correspondenceAddress,
        String phoneNumber
    ) {
        TheirDetails defendantDetailsInClaim = new IndividualDetails(
            null, address, null, null, correspondenceAddress, null
        );

        ClaimData claimData = SampleClaimData.builder()
            .withDefendant(defendantDetailsInClaim)
            .build();

        return SampleClaim.builder()
            .withClaimData(claimData)
            .build();
    }

    private Response buildResponseWithContactDetails(
        Address address,
        Address correspondenceAddress,
        String phoneNumber
    ) {
        Party defendantDetails = new Individual(
            null, address, correspondenceAddress, phoneNumber, null, null
        );

        return SampleResponse.FullDefence.builder()
            .withDefendantDetails(defendantDetails)
            .build();
    }

    private Address getAddress1() {
        return null;
    }

    private Address getAddress2() {
        return null;
    }

    private String getPhoneNumber1() {
        return "1";
    }

    private String getPhoneNumber2() {
        return "2";
    }
}
