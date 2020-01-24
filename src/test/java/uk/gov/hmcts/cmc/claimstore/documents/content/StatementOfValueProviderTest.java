package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.StatementOfValueContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.amount.NotKnown;
import uk.gov.hmcts.cmc.domain.models.particulars.PersonalInjury;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.documents.content.StatementOfValueProvider.ALSO_HOUSING_DISREPAIR;
import static uk.gov.hmcts.cmc.claimstore.documents.content.StatementOfValueProvider.CAN_NOT_STATE;
import static uk.gov.hmcts.cmc.claimstore.documents.content.StatementOfValueProvider.HOUSING_DISREPAIR;
import static uk.gov.hmcts.cmc.claimstore.documents.content.StatementOfValueProvider.PERSONAL_INJURY;
import static uk.gov.hmcts.cmc.claimstore.documents.content.StatementOfValueProvider.PERSONAL_INJURY_DAMAGES;
import static uk.gov.hmcts.cmc.claimstore.documents.content.StatementOfValueProvider.RECOVER_UP_TO;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.MORE_THAN_THOUSAND_POUNDS;

@RunWith(MockitoJUnitRunner.class)
public class StatementOfValueProviderTest {

    private final StatementOfValueProvider statementOfValueProvider = new StatementOfValueProvider();

    @Test
    public void shouldCreateContentWithAmountRange() {
        //given
        Claim claim = buildClaimModel(
            SampleClaimData.builder()
                .withAmount(
                    SampleAmountRange.builder()
                        .lowerValue(BigDecimal.valueOf(200.95))
                        .higherValue(BigDecimal.valueOf(100.50))
                        .build()
                ).build());

        //when
        StatementOfValueContent statementOfValueContent = statementOfValueProvider.create(claim);

        //then
        assertThat(statementOfValueContent.getClaimValue()).contains("100.50");
        assertThat(statementOfValueContent.getClaimValue()).contains("200.95");
    }

    @Test
    public void shouldCreateContentWithAmountRangeWithoutLowerValue() {
        //given
        Claim claim = buildClaimModel(
            SampleClaimData.builder()
                .withAmount(
                    SampleAmountRange.builder()
                        .lowerValue(null)
                        .higherValue(BigDecimal.valueOf(100.50))
                        .build()
                ).build());

        //when
        StatementOfValueContent statementOfValueContent = statementOfValueProvider.create(claim);

        //then
        assertThat(statementOfValueContent.getClaimValue()).contains(String.format(RECOVER_UP_TO, "£100.50"));
        assertThat(statementOfValueContent.getClaimValue())
            .doesNotContain("The claimant estimates the claim to be worth more than");
    }

    @Test
    public void shouldCreateContentWithAmountNotKnown() {
        //given
        Claim claim = buildClaimModel(SampleClaimData.builder().withAmount(new NotKnown()).build());

        //when
        StatementOfValueContent statementOfValueContent = statementOfValueProvider.create(claim);

        //then
        assertThat(statementOfValueContent.getClaimValue()).contains(CAN_NOT_STATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorForInvalidAmountType() {
        //given
        Claim claim = SampleClaim.builder().build();

        //when
        statementOfValueProvider.create(claim);

    }

    @Test
    public void shouldCreateContentWithPersonalInjury() {
        //given
        Claim claim = buildClaimModel(
            SampleClaimData.builder()
                .withAmount(
                    SampleAmountRange.builder()
                        .lowerValue(BigDecimal.valueOf(200.95))
                        .higherValue(BigDecimal.valueOf(100.50))
                        .build()
                )
                .withPersonalInjury(new PersonalInjury(MORE_THAN_THOUSAND_POUNDS))
                .withHousingDisrepair(null)
                .build());

        String expected = String.format(PERSONAL_INJURY_DAMAGES, MORE_THAN_THOUSAND_POUNDS.getDisplayValue());

        //when
        StatementOfValueContent statementOfValueContent = statementOfValueProvider.create(claim);

        //then
        assertThat(statementOfValueContent.getPersonalInjury()).contains(PERSONAL_INJURY);
        assertThat(statementOfValueContent.getPersonalInjury()).contains(expected);
    }

    @Test
    public void shouldCreateContentWithHousingDisrepair() {
        //given
        Claim claim = buildClaimModel(
            SampleClaimData.builder()
                .withPersonalInjury(null)
                .withAmount(SampleAmountRange.builder()
                    .lowerValue(BigDecimal.valueOf(200.95))
                    .higherValue(BigDecimal.valueOf(100.50))
                    .build()
                ).build());

        //when
        StatementOfValueContent statementOfValueContent = statementOfValueProvider.create(claim);

        //then
        assertThat(statementOfValueContent.getHousingDisrepair()).contains(HOUSING_DISREPAIR);
    }

    @Test
    public void shouldCreateContentWithHousingDisrepairAndPersonalInjury() {
        //given
        Claim claim = buildClaimModel(
            SampleClaimData.builder()
                .withAmount(
                    SampleAmountRange.builder()
                        .lowerValue(BigDecimal.valueOf(200.95))
                        .higherValue(BigDecimal.valueOf(100.50))
                        .build()
                ).build());

        //when
        StatementOfValueContent statementOfValueContent = statementOfValueProvider.create(claim);

        //then
        assertThat(statementOfValueContent.getHousingDisrepair()).contains(ALSO_HOUSING_DISREPAIR);
    }

    private Claim buildClaimModel(ClaimData claimData) {
        return SampleClaim.builder().withClaimData(claimData).build();
    }
}
