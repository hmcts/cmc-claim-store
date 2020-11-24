package uk.gov.hmcts.cmc.domain.models;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

@ExtendWith(MockitoExtension.class)
class AddressTest {

    @Test
    void shouldBeSuccessfulValidationForCorrectAddress() {
        //given
        Address address = SampleAddress.builder().build();
        //when
        Set<String> response = validate(address);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    void shouldBeInvalidForNullLineOne() {
        //given
        Address address = SampleAddress.builder()
            .line1(null)
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("line1 : Address Line1 should not be empty");
    }

    @Test
    void shouldBeInvalidForEmptyLineOne() {
        //given
        Address address = SampleAddress.builder()
            .line1("")
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("line1 : Address Line1 should not be empty");
    }

    @Test
    void shouldBeInvalidForTooLongLineOne() {
        //given
        Address address = SampleAddress.builder()
            .line1(StringUtils.repeat("a", 101))
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("line1 : Address Line1 should not be longer than 100 characters");
    }

    @Test
    void shouldBeInvalidForTooLongLineTwo() {
        //given
        Address address = SampleAddress.builder()
            .line2(StringUtils.repeat("a", 101))
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("line2 : Address Line2 should not be longer than 100 characters");
    }

    @Test
    void shouldBeInvalidForTooLongLineThree() {
        //given
        Address address = SampleAddress.builder()
            .line3(StringUtils.repeat("a", 101))
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("line3 : Address Line3 should not be longer than 100 characters");
    }

    @Test
    void shouldBeInvalidForEmptyCity() {
        //given
        Address address = SampleAddress.builder()
            .city("")
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("city : City/town should not be empty");
    }

    @Test
    void shouldBeInvalidForTooLongCity() {
        //given
        Address address = SampleAddress.builder()
            .city(StringUtils.repeat("a", 101))
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("city : City should not be longer than 100 characters");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"SW123456"})
    void shouldBeInvalidForNullPostcode(String input) {
        //given
        Address address = SampleAddress.builder()
            .postcode(input)
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .containsAnyOf("postcode : may not be null", "postcode : Postcode is not of valid format",
                "postcode : Postcode is not of valid format");
    }

}
