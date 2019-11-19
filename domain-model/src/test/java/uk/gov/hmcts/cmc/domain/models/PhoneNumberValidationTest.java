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
        Individual party = individualWithPhone("(+44) (0)7931232313");
        //when
        Set<String> errors = validate(party);
        //then
        assertThat(errors).isEmpty();
    }

    private Individual individualWithPhone(String phone) {
        return SampleParty.builder()
            .withPhone(phone)
            .individual();
    }

    @Test
    public void shouldBeSuccessfulValidationForPhoneNumberOfType2() {
        //given
        Individual party = individualWithPhone("004407931232313");
        //when
        Set<String> errors = validate(party);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForPhoneNumberOfType3() {
        //given
        Individual party = individualWithPhone("07931232313");
        //when
        Set<String> errors = validate(party);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForPhoneNumbeWithRandomCharacter() {
        //given
        Individual party = individualWithPhone("0793123231*");
        //when
        Set<String> errors = validate(party);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForPhoneNumberOfType4() {
        //given
        Individual party = individualWithPhone("(0044) (0)7931232313");
        //when
        Set<String> errors = validate(party);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeValidWhenNumberIsNull() {
        //given
        Individual party = individualWithPhone(null);
        //when
        Set<String> errors = validate(party);
        //then
        assertThat(errors).isNotNull()
            .hasSize(0);
    }

}
