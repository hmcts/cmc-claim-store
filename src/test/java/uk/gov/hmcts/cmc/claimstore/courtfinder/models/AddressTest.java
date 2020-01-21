package uk.gov.hmcts.cmc.claimstore.courtfinder.models;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AddressTest {

    @Test
    void shouldStripAddressFromSpecialChars() {
        Address address = Address.builder()
            .addressLines(Arrays.asList("aaa\r", "bbb\r"))
            .postcode("EC2Y3DD")
            .town("London")
            .build();
        assertThat(address.toString()).isEqualTo("aaa\nbbb\nEC2Y3DD\nLondon");
    }

}
