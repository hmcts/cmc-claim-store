package uk.gov.hmcts.cmc.domain.models.legalrep;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.legalrep.SampleContactDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class ContactDetailsTest {

    @Test
    public void shouldBeValidForValidObject() {
        ContactDetails contactDetails = SampleContactDetails.validDefaults();

        Set<String> validationErrors = validate(contactDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeValidForNullValues() {
        ContactDetails contactDetails = SampleContactDetails.builder()
            .withPhone(null)
            .withEmail(null)
            .withDxNumber(null)
            .build();

        Set<String> validationErrors = validate(contactDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeValidForEmptyValues() {
        ContactDetails contactDetails = SampleContactDetails.builder()
            .withEmail("")
            .withDxNumber("")
            .build();

        Set<String> validationErrors = validate(contactDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void phoneNumberShouldNotBeValidForEmptyValue() {
        ContactDetails contactDetails = SampleContactDetails.builder()
            .withPhone("")
            .build();

        Set<String> validationErrors = validate(contactDetails);

        assertThat(validationErrors).containsOnly("phone : must be a valid UK phone number");
    }

    @Test
    public void shouldBeInvalidForInvalidPhoneNumber() {
        ContactDetails contactDetails = SampleContactDetails.builder()
            .withPhone("123")
            .build();

        Set<String> validationErrors = validate(contactDetails);

        assertThat(validationErrors).containsOnly("phone : must be a valid UK phone number");
    }

    @Test
    public void shouldBeInvalidForInvalidEmail() {
        ContactDetails contactDetails = SampleContactDetails.builder()
            .withEmail("this is not a valid email")
            .build();

        Set<String> validationErrors = validate(contactDetails);

        assertThat(validationErrors).containsOnly("email : not a well-formed email address");
    }

    @Test
    public void shouldBeValidFor255CharsDxNumber() {
        ContactDetails contactDetails = SampleContactDetails.builder()
            .withDxNumber(StringUtils.repeat("X", 255))
            .build();

        Set<String> validationErrors = validate(contactDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeInvalidForTooLongDxNumber() {
        ContactDetails contactDetails = SampleContactDetails.builder()
            .withDxNumber(StringUtils.repeat("X", 256))
            .build();

        Set<String> validationErrors = validate(contactDetails);

        assertThat(validationErrors).containsOnly("dxAddress : must be at most 255 characters");
    }

}
