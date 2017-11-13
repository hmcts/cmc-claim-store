package uk.gov.hmcts.cmc.claimstore.models;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleAddress;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.BeanValidator.validate;

public class AddressTest {

    @Test
    public void shouldBeSuccessfulValidationForCorrectAddress() {
        //given
        Address address = SampleAddress.validDefaults();
        //when
        Set<String> response = validate(address);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForNullLineOne() {
        //given
        Address address = SampleAddress.builder()
            .withLine1(null)
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("line1 : Address Line1 should not be empty");
    }

    @Test
    public void shouldBeInvalidForEmptyLineOne() {
        //given
        Address address = SampleAddress.builder()
            .withLine1("")
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("line1 : Address Line1 should not be empty");
    }

    @Test
    public void shouldBeInvalidForTooLongLineOne() {
        //given
        Address address = SampleAddress.builder()
            .withLine1(StringUtils.repeat("a", 101))
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("line1 : Address Line1 should not be longer than 100 characters");
    }

    @Test
    public void shouldBeInvalidForTooLongLineTwo() {
        //given
        Address address = SampleAddress.builder()
            .withLine2(StringUtils.repeat("a", 101))
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("line2 : Address Line2 should not be longer than 100 characters");
    }

    @Test
    public void shouldBeInvalidForEmptyCity() {
        //given
        Address address = SampleAddress.builder()
            .withCity("")
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("city : City/town should not be empty");
    }

    @Test
    public void shouldBeInvalidForTooLongCity() {
        //given
        Address address = SampleAddress.builder()
            .withCity(StringUtils.repeat("a", 101))
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("city : City should not be longer than 100 characters");
    }

    @Test
    public void shouldBeInvalidForNullPostcode() {
        //given
        Address address = SampleAddress.builder()
            .withPostcode(null)
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("postcode : Postcode should not be empty");
    }

    @Test
    public void shouldBeInvalidForEmptyPostcode() {
        //given
        Address address = SampleAddress.builder()
            .withPostcode("")
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("postcode : Postcode should not be empty");
    }

    @Test
    public void shouldBeInvalidForTooLongPostcode() {
        //given
        Address address = SampleAddress.builder()
            .withPostcode(StringUtils.repeat("A", 9))
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("postcode : Postcode should not be longer than 8 characters");
    }

}
