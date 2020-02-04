package uk.gov.hmcts.cmc.domain.models.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

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
        assertThat(other.getOffer().map(Offer::getContent).orElse(null)).isEqualTo("I will fix the leaking roof");
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

    @Test
    public void shouldConvertRejectionJsonToJavaObject() throws IOException {
        //when
        PartyStatement other = jsonToModel("/partyStatement/rejection.json");

        //then
        assertThat(other.getType()).isEqualTo(StatementType.REJECTION);
        assertThat(other.getMadeBy()).isEqualTo(MadeBy.DEFENDANT);
        assertThat(other.getOffer().isPresent()).isEqualTo(false);
    }

    private static PartyStatement jsonToModel(String path) throws IOException {
        String json = new ResourceReader().read(path);
        return mapper.readValue(json, PartyStatement.class);
    }
}
