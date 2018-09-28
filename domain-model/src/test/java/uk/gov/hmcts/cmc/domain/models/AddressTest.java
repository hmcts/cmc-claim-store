package uk.gov.hmcts.cmc.domain.models;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class AddressTest {

    @Test
    public void shouldBeSuccessfulValidationForCorrectAddress() {
        //given
        Address address = SampleAddress.builder().build();
        //when
        Set<String> response = validate(address);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForNullLineOne() {
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
    public void shouldBeInvalidForEmptyLineOne() {
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
    public void shouldBeInvalidForTooLongLineOne() {
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
    public void shouldBeInvalidForTooLongLineTwo() {
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
    public void shouldBeInvalidForTooLongLineThree() {
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
    public void shouldBeInvalidForEmptyCity() {
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
    public void shouldBeInvalidForTooLongCity() {
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

    @Test
    public void shouldBeInvalidForNullPostcode() {
        //given
        Address address = SampleAddress.builder()
            .postcode(null)
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("postcode : may not be null");
    }

    @Test
    public void shouldBeInvalidForEmptyPostcode() {
        //given
        Address address = SampleAddress.builder()
            .postcode("")
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("postcode : Postcode is not of valid format");
    }

    @Test
    public void shouldBeInvalidForInvalidPostcode() {
        //given
        Address address = SampleAddress.builder()
            .postcode("SW123456")
            .build();
        //when
        Set<String> errors = validate(address);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("postcode : Postcode is not of valid format");
    }

}
