package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimDataDeprecableTest {


    private InterestDate interestDate = SampleInterestDate.validDefaults();

    @Test
    public void shouldReturnTheInterestDateFromClaimDataObject() {
        Interest interest = SampleInterest
                .builder()
                .withInterestDate(null)
                .build();
        ClaimData claimData = SampleClaimData
                .builder()
                .withInterestDate(interestDate)
                .withInterest(interest)
                .build();
        assertThat(claimData.getInterestDate()).isEqualTo(interestDate);
    }

    @Test
    public void shouldReturnTheInterestDateFromInterestObject() {
        Interest interest = SampleInterest
                .builder()
                .withInterestDate(interestDate)
                .build();
        ClaimData claimData = SampleClaimData
                .builder()
                .withInterestDate(null)
                .withInterest(interest)
                .build();
        assertThat(claimData.getInterestDate()).isEqualTo(interestDate);
    }

    @Test
    public void shouldReturnTheInterestDateFromClaimDataObjectWhenInBothPlaces() {
        InterestDate anotherInterestDate = SampleInterestDate.validDefaults();

        Interest interest = SampleInterest
                .builder()
                .withInterestDate(anotherInterestDate)
                .build();
        ClaimData claimData = SampleClaimData
                .builder()
                .withInterestDate(interestDate)
                .withInterest(interest)
                .build();
        assertThat(claimData.getInterestDate()).isEqualTo(interestDate);
    }

    @Test
    public void shouldReturnNullWhenInterestDateAndInterestAreNull() {
        ClaimData claimData = SampleClaimData
            .builder()
            .withInterestDate(null)
            .withInterest(null)
            .build();
        assertThat(claimData.getInterestDate()).isNull();
    }

}
