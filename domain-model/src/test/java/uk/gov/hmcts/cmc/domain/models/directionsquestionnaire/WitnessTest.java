package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class WitnessTest {

    @Test
    public void shouldBeSuccessfulValidationForWitness() {
        Witness witness = Witness
            .builder()
            .selfWitness(YES)
            .noOfOtherWitness(1)
            .build();

        Set<String> response = validate(witness);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldBeValidationMessagesWhenSelfWitnessIsNull() {
        Witness witness = Witness
            .builder()
            .noOfOtherWitness(1)
            .build();

        Set<String> response = validate(witness);

        assertThat(response)
            .hasSize(1)
            .contains("selfWitness : may not be null");
    }

    @Test
    public void shouldBeValidationMessagesWhenNoOfOtherWitnessIsLessThanMin() {
        Witness witness = Witness
            .builder()
            .selfWitness(YES)
            .noOfOtherWitness(0)
            .build();

        Set<String> response = validate(witness);

        assertThat(response)
            .hasSize(1)
            .contains("noOfOtherWitness : must be greater than or equal to 1");
    }

    @Test
    public void shouldBeValidationMessagesWhenNoOfOtherWitnessIsLongerThanMax() {
        Witness witness = Witness
            .builder()
            .selfWitness(YES)
            .noOfOtherWitness(101)
            .build();

        Set<String> response = validate(witness);

        assertThat(response)
            .hasSize(1)
            .contains("noOfOtherWitness : must be less than or equal to 100");
    }
}
