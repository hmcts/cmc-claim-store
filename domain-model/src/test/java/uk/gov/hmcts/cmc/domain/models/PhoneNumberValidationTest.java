package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class PhoneNumberValidationTest {

    @Test
    public void shouldBeSuccessfulValidationForPhoneNumberOfType1() {
        //given
        Individual party = individualWithPhoneNumber("(+44) (0)7931232313");
        //when
        Set<String> errors = validate(party);
        //then
        assertThat(errors).isEmpty();
    }

    private Individual individualWithPhoneNumber(String phoneNumber) {
        return SampleParty.builder()
            .withPhoneNumber(phoneNumber)
            .individual();
    }

    @Test
    public void shouldBeSuccessfulValidationForPhoneNumberOfType2() {
        //given
        Individual party = individualWithPhoneNumber("004407931232313");
        //when
        Set<String> errors = validate(party);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForPhoneNumberOfType3() {
        //given
        Individual party = individualWithPhoneNumber("07931232313");
        //when
        Set<String> errors = validate(party);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForPhoneNumbeWithRandomCharacter() {
        //given
        Individual party = individualWithPhoneNumber("0793123231*");
        //when
        Set<String> errors = validate(party);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForPhoneNumberOfType4() {
        //given
        Individual party = individualWithPhoneNumber("(0044) (0)7931232313");
        //when
        Set<String> errors = validate(party);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeValidWhenNumberIsNull() {
        //given
        Individual party = individualWithPhoneNumber(null);
        //when
        Set<String> errors = validate(party);
        //then
        assertThat(errors).isNotNull()
            .hasSize(0);
    }

}
