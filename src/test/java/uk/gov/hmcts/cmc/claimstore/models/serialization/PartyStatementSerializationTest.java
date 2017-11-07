package uk.gov.hmcts.cmc.claimstore.models.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;
import uk.gov.hmcts.cmc.claimstore.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.claimstore.models.offers.StatementType;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class PartyStatementSerializationTest {

    private static final ObjectMapper mapper = new JacksonConfiguration().objectMapper();

    @Test
    public void shouldConvertOfferJsonToJavaObject() throws IOException {
        //when
        PartyStatement other = jsonToModel("/partyStatement/offer.json");

        //then
        assertThat(other.getType()).isEqualTo(StatementType.OFFER);
        assertThat(other.getMadeBy()).isEqualTo(MadeBy.DEFENDANT);
        assertThat(other.getOffer().get().getContent()).isEqualTo("I will fix the leaking roof");
    }

    @Test
    public void shouldConvertAcceptationJsonToJavaObject() throws IOException {
        //when
        PartyStatement other = jsonToModel("/partyStatement/acceptation.json");

        //then
        assertThat(other.getType()).isEqualTo(StatementType.ACCEPTATION);
        assertThat(other.getMadeBy()).isEqualTo(MadeBy.CLAIMANT);
        assertThat(other.getOffer().isPresent()).isEqualTo(false);
    }

    private static PartyStatement jsonToModel(final String path) throws IOException {
        final String json = new ResourceReader().read(path);
        return mapper.readValue(json, PartyStatement.class);
    }
}
