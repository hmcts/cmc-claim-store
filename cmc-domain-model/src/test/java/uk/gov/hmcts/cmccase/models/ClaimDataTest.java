package uk.gov.hmcts.cmccase.models;

import org.junit.Test;
import uk.gov.hmcts.cmccase.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmccase.models.sampledata.SampleInterest;
import uk.gov.hmcts.cmccase.models.sampledata.SampleInterestDate;
import uk.gov.hmcts.cmccase.models.sampledata.SampleParty;
import uk.gov.hmcts.cmccase.models.sampledata.SampleTheirDetails;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmccase.models.sampledata.SampleClaimData.noInterest;
import static uk.gov.hmcts.cmccase.utils.BeanValidator.validate;

public class ClaimDataTest {

    private InterestDate invalidDate = SampleInterestDate.builder()
        .withType(null)
        .withDate(null)
        .withReason(null)
        .build();

    @Test
    public void shouldHaveNoValidationMessagesWhenInterestTypeIsNoInterest() {
        //given
        ClaimData claimData = noInterest();
        //when
        Set<String> response = validate(claimData);
        //then
        assertThat(response).isNotNull().hasSize(0);
    }

    @Test
    public void shouldBeInvalidWhenGivenStandardInterestWithInvalidDate() {
        //given
        ClaimData claimData = SampleClaimData.builder()
            .withInterest(SampleInterest.standard())
            .withInterestDate(invalidDate)
            .build();
        //when
        Set<String> response = validate(claimData);
        //then
        assertThat(response)
            .hasSize(1)
            .contains("interestDate : reason : may not be empty, type : may not be null");
    }

    @Test
    public void shouldBeInvalidWhenGivenCustomInterestWithInvalidDate() {
        //given
        ClaimData claimData = SampleClaimData.builder()
            .withInterest(SampleInterest.builder()
                .withType(Interest.InterestType.DIFFERENT)
                .build())
            .withInterestDate(invalidDate)
            .build();
        //when
        Set<String> response = validate(claimData);
        //then
        assertThat(response)
            .hasSize(1)
            .contains("interestDate : reason : may not be empty, type : may not be null");
    }

    @Test
    public void shouldBeValidWhenGivenNoInterestWithInvalidInterestDate() {
        ClaimData claimData = SampleClaimData.builder()
            .withInterest(SampleInterest.noInterest())
            .withInterestDate(invalidDate)
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeInvalidWhenGivenNullDefendants() {
        ClaimData claimData = SampleClaimData.builder()
            .withDefendants(null)
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).containsOnly("defendants : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenNoDefendants() {
        ClaimData claimData = SampleClaimData.builder()
            .clearDefendants()
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).containsOnly("defendants : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenNullDefendant() {
        ClaimData claimData = SampleClaimData.builder()
            .withDefendant(null)
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).containsOnly("defendants[0] : may not be null");
    }

    @Test
    public void shouldBeInvalidWhenGivenInvalidDefendant() {
        ClaimData claimData = SampleClaimData.builder()
            .withDefendant(SampleTheirDetails.builder()
                .withName("")
                .individualDetails())
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).containsOnly("defendants[0].name : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenTooManyDefendants() {
        ClaimData claimData = SampleClaimData.builder()
            .clearDefendants()
            .addDefendants(SampleTheirDetails.builder().individualDetails(21))
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).containsOnly("defendants : at most 20 defendants are supported");
    }

    @Test
    public void shouldBeValidWhenGivenTwentyDefendants() {
        ClaimData claimData = SampleClaimData.builder()
            .clearDefendants()
            .addDefendants(SampleTheirDetails.builder().individualDetails(20))
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).isEmpty();
    }

    @Test
    public void getDefendantShouldReturnDefendantWhenOnlyOneIsSet() {
        ClaimData claimData = SampleClaimData.validDefaults();
        assertThat(claimData.getDefendant()).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void getDefendantShouldThrowIllegalStateWhenThereIsMoreThanOneDefendant() {
        ClaimData claimData = SampleClaimData.builder()
            .clearDefendants()
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .build();

        claimData.getDefendant();
    }

    @Test
    public void shouldBeInvalidWhenGivenNullClaimants() {
        ClaimData claimData = SampleClaimData.builder()
            .withClaimants(null)
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).containsOnly("claimants : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenNoClaimants() {
        ClaimData claimData = SampleClaimData.builder()
            .clearClaimants()
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).containsOnly("claimants : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenNullClaimant() {
        ClaimData claimData = SampleClaimData.builder()
            .withClaimant(null)
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).containsOnly("claimants[0] : may not be null");
    }

    @Test
    public void shouldBeInvalidWhenGivenInvalidClaimant() {
        ClaimData claimData = SampleClaimData.builder()
            .withClaimant(SampleParty.builder()
                .withName("")
                .individual())
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).containsOnly("claimants[0].name : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenInvalidClaimantAddress() {
        ClaimData claimData = SampleClaimData.builder()
            .withClaimant(SampleParty.builder()
                .withAddress(null)
                .individual())
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).containsOnly("claimants[0].address : may not be null");
    }

    @Test
    public void shouldBeInvalidWhenGivenTooManyClaimants() {
        ClaimData claimData = SampleClaimData.builder()
            .clearClaimants()
            .addClaimants(SampleParty.builder().individualDetails(21))
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).containsOnly("claimants : at most 20 claimants are supported");
    }

    @Test
    public void shouldBeValidWhenGivenTwentyClaimants() {
        ClaimData claimData = SampleClaimData.builder()
            .clearClaimants()
            .addClaimants(SampleParty.builder().individualDetails(20))
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).isEmpty();
    }

    @Test
    public void getClaimantShouldReturnDefendantWhenOnlyOneIsSet() {
        ClaimData claimData = SampleClaimData.validDefaults();
        assertThat(claimData.getClaimant()).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void getClaimantShouldThrowIllegalStateWhenThereIsMoreThanOneClaimant() {
        ClaimData claimData = SampleClaimData.builder()
            .clearClaimants()
            .addClaimant(SampleParty.builder().individual())
            .addClaimant(SampleParty.builder().individual())
            .build();

        claimData.getClaimant();
    }

    @Test
    public void shouldConvertFeesToPound() {
        ClaimData claimData = SampleClaimData.builder().withFeeAmount(BigInteger.valueOf(456712)).build();
        assertThat(claimData.getFeesPaidInPound()).isEqualTo(new BigDecimal("4567.12"));
    }
}
