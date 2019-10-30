package uk.gov.hmcts.cmc.domain.models;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class PartyTest {

    @Test
    public void shouldReturnValidationErrorsWhenNameIsNull() {
        Party party = SampleParty.builder()
            .withName(null)
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("name : may not be empty");
    }

    @Test
    public void shouldReturnValidationErrorsWhenNameIsEmpty() {
        Party party = SampleParty.builder()
            .withName("")
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("name : may not be empty");
    }

    @Test
    public void shouldReturnValidationErrorsWhenNameIsTooLong() {
        Party party = SampleParty.builder()
            .withName(StringUtils.repeat("nana", 200))
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("name : may not be longer than 255 characters");
    }

    @Test
    public void shouldReturnValidationErrorsWhenGivenNullAddress() {
        Party party = SampleParty.builder()
            .withAddress(null)
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("address : may not be null");
    }

    @Test
    public void shouldReturnValidationErrorsWhenGivenInvalidAddress() {
        Party party = SampleParty.builder()
            .withAddress(SampleAddress.builder()
                .postcode("")
                .build())
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("address.postcode : Postcode is not of valid format");
    }

    @Test
    public void shouldBeValidWhenGivenNotGivenCorrespondenceAddress() {
        Party party = SampleParty.builder()
            .withCorrespondenceAddress(null)
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldReturnValidationErrorsWhenGivenInvalidCorrespondenceAddress() {
        Party party = SampleParty.builder()
            .withCorrespondenceAddress(SampleAddress.builder()
                .postcode("")
                .build())
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("correspondenceAddress.postcode : Postcode is not of valid format");
    }

    @Test
    public void shouldReturnNoValidationErrorsWhenGivenNullPhone() {
        Party party = SampleParty.builder()
            .withPhone(null)
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(0);
    }

    @Test
    public void shouldReturnNoValidationErrorsWhenGivenValidPhone() {
        Party party = SampleParty.builder()
            .withPhone("07987654321")
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(0);
    }

    @Test
    public void shouldReturnValidationErrorsWhenGivenInvalidPhone() {
        Party party = SampleParty.builder()
            .withPhone("1234567890123456789012345678901")
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("phone : may not be longer than 30 characters");
    }

}
