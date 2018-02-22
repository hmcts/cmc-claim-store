package uk.gov.hmcts.cmc.domain.models;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class NameTest {

    @Test
    public void shouldBeSuccessfulValidationForCorrectName() {
        //given
        ClaimData claimData = SampleClaimData.validDefaults();
        //when
        Set<String> response = validate(claimData);
        //then
        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldBeValidationMessagesForNameLongerThanMax() {
        //given
        ClaimData claimData = SampleClaimData.builder()
            .clearClaimants()
            .addClaimant(SampleParty.builder()
                .withName(StringUtils.repeat('a', 256))
                .individual())
            .build();

        //when
        Set<String> messages = validate(claimData);

        //then
        assertThat(messages)
            .hasSize(1)
            .contains("claimants[0].name : may not be longer than 255 characters");
    }

    @Test
    public void shouldBeValidationMessagesForBlankName() {
        //given
        ClaimData claimData = SampleClaimData.builder()
            .clearClaimants()
            .addClaimant(SampleParty.builder()
                .withName("")
                .individual())
            .build();

        //when
        Set<String> messages = validate(claimData);

        //then
        assertThat(messages)
            .hasSize(1)
            .contains("claimants[0].name : may not be empty");
    }
}
