package uk.gov.hmcts.cmc.claimstore.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleInterest;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleInterestDate;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleTheirDetails;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData.noInterest;
import static uk.gov.hmcts.cmc.claimstore.utils.BeanValidator.validate;

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
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .build();

        Set<String> errors = validate(claimData);

        assertThat(errors).containsOnly("defendants : at most 20 defendants are supported");
    }

    @Test
    public void shouldBeValidWhenGivenFourDefendants() {
        ClaimData claimData = SampleClaimData.builder()
            .clearDefendants()
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
            .addDefendant(SampleTheirDetails.builder().individualDetails())
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
    public void shouldConvertFeesToPound() {
        ClaimData claimData = SampleClaimData.builder().withFeeAmount(BigInteger.valueOf(456712)).build();
        assertThat(claimData.getFeesPaidInPound()).isEqualTo(new BigDecimal("4567.12"));
    }
}
