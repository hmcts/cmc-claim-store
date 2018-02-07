package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class DateOfBirthTest {

    @Test
    public void shouldReturnValidationMessageForFutureDate() {
        //given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Individual claimant = SampleParty.builder()
            .withDateOfBirth(futureDate)
            .individual();

        //when
        Set<String> errors = validate(claimant);

        //then
        assertThat(errors)
            .hasSize(1)
            .contains("dateOfBirth : Age must be between 18 and 150");
    }

    @Test
    public void shouldReturnValidationMessageForAgeLessThan18() {
        //given
        LocalDate under18Years = LocalDate.now().minusYears(2);
        Individual claimant = SampleParty.builder()
            .withDateOfBirth(under18Years)
            .individual();

        //when
        Set<String> errors = validate(claimant);

        //then
        assertThat(errors)
            .hasSize(1)
            .contains("dateOfBirth : Age must be between 18 and 150");
    }

    @Test
    public void shouldReturnValidationMessageForAgeMoreThan150() {
        //given
        LocalDate over150Years = LocalDate.now().minusYears(151);
        Individual claimant = SampleParty.builder()
            .withDateOfBirth(over150Years)
            .individual();

        //when
        Set<String> errors = validate(claimant);

        //then
        assertThat(errors)
            .hasSize(1)
            .contains("dateOfBirth : Age must be between 18 and 150");
    }
}
