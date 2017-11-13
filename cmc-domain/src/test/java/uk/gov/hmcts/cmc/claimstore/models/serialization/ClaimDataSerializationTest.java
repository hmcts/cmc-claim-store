package uk.gov.hmcts.cmc.claimstore.models.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleInterestDate;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleParty;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimDataSerializationTest {

    @Test
    public void shouldConvertJsonToJava() throws IOException {
        //given
        String input = new ResourceReader().read("/claim-application.json");
        ObjectMapper mapper = new JacksonConfiguration().objectMapper();

        //when
        ClaimData claimData = mapper.readValue(input, ClaimData.class);

        //then
        ClaimData other = SampleClaimData.builder()
            .withExternalId(UUID.fromString("9f49d8df-b734-4e86-aeb6-e22f0c2ca78d"))
            .withInterestDate(SampleInterestDate.builder()
                .withDate(LocalDate.of(2015, 2, 2))
                .build())
            .withExternalReferenceNumber(null)
            .withPreferredCourt(null)
            .withFeeAccountNumber(null)
            .withHousingDisrepair(null)
            .withPersonalInjury(null)
            .withStatementOfTruth(null)
            .clearClaimants()
            .addClaimant(SampleParty.builder().withRepresentative(null).individual())
            .withDefendant(SampleTheirDetails.builder()
                .withRepresentative(null)
                .withServiceAddress(null)
                .individualDetails())
            .build();

        assertThat(claimData).isEqualTo(other);
    }
}
