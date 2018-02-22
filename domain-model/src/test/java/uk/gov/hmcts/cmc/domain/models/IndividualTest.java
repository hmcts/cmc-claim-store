package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class IndividualTest {

    @Test
    public void shouldNotRaiseValidationErrorWhenDateOfBirthIsNotProvided() {
        Individual individual = SampleParty.builder()
            .withDateOfBirth(null)
            .individual();

        Set<String> errorMessages = validate(individual);

        assertThat(errorMessages).hasSize(0);
    }

    @Test
    public void shouldRaiseValidationErrorWhenDateOfBirthIsFromTheFuture() {
        Individual individual = SampleParty.builder()
            .withDateOfBirth(LocalDate.now().plusDays(10))
            .individual();

        Set<String> errorMessages = validate(individual);

        assertThat(errorMessages).hasSize(1).contains("dateOfBirth : Age must be between 18 and 150");
    }

}
