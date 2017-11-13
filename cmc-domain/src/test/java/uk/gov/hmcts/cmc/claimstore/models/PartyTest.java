package uk.gov.hmcts.cmc.claimstore.models;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleParty;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.BeanValidator.validate;

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
                .withPostcode("")
                .build())
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("address.postcode : Postcode should not be empty");
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
                .withPostcode("")
                .build())
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("correspondenceAddress.postcode : Postcode should not be empty");
    }

    @Test
    public void shouldReturnNoValidationErrorsWhenGivenNullMobilePhone() {
        Party party = SampleParty.builder()
            .withMobilePhone(null)
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(0);
    }

    @Test
    public void shouldReturnValidationErrorsWhenGivenEmptyMobilePhone() {
        Party party = SampleParty.builder()
            .withMobilePhone("")
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("mobilePhone : Mobile number is not valid UK number");
    }

    @Test
    public void shouldReturnValidationErrorsWhenGivenInvalidMobilePhone() {
        Party party = SampleParty.builder()
            .withMobilePhone("432423")
            .party();

        Set<String> validationErrors = validate(party);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("mobilePhone : Mobile number is not valid UK number");
    }

}
