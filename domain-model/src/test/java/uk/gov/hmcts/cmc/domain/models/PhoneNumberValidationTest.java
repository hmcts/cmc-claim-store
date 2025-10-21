package uk.gov.hmcts.cmc.domain.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

@ExtendWith(MockitoExtension.class)
class PhoneNumberValidationTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"(+44) (0)7931232313", "004407931232313", "07931232313",
        "0793123231*", "(0044) (0)7931232313"})
    void shouldBeSuccessfulValidationForPhoneNumberOfType1(String input) {
        //given
        Individual party = individualWithPhone(input);
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
    void shouldBeValidWhenNumberIsNull() {
        //given
        Individual party = individualWithPhone(null);
        //when
        Set<String> errors = validate(party);
        //then
        assertThat(errors).isNotNull()
            .hasSize(0);
    }

}
