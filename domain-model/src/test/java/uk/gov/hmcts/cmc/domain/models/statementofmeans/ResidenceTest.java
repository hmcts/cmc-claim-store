package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence.ResidenceType.OTHER;

public class ResidenceTest {
    public static Residence.ResidenceBuilder newSampleOfResidenceBuilder() {
        return Residence.builder()
                .type(OTHER)
                .otherDetail("Other details");
    }

    @Test
    public void shouldBeSuccessfulValidationForCorrectResidence() {
        //given
        Residence residence = newSampleOfResidenceBuilder().build();
        //when
        Set<String> response = validate(residence);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForAllNullFields() {
        //given
        //given
        Residence residence = Residence.builder().build();
        //when
        Set<String> errors = validate(residence);
        //then
        assertThat(errors)
                .hasSize(1);
    }

    @Test
    public void shouldBeInvalidForNullTypeOfResidence() {
        //given
        Residence residence = Residence.builder().build();
        //when
        Set<String> errors = validate(residence);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("type : may not be null");
    }
}