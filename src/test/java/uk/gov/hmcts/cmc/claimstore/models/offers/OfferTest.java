package uk.gov.hmcts.cmc.claimstore.models.offers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class OfferTest {

    private ObjectMapper mapper = new JacksonConfiguration().objectMapper();
    private ResourceReader resourceReader = new ResourceReader();

    @Test
    public void offerResponseShouldBePendingForNewInstance() {
        Offer offer = new Offer("I offer to repair the roof", LocalDate.now());
        assertThat(offer.getResponse()).isEqualByComparingTo(Response.PENDING);
    }

    @Test
    public void offerResponseShouldBePendingForNewInstanceConstructedFromJSON() throws IOException {
        Offer offerFromJSON = mapper.readValue(resourceReader.read("/new-offer.json"), Offer.class);
        assertThat(offerFromJSON.getResponse()).isEqualByComparingTo(Response.PENDING);
    }

    @Test
    public void counterOfferShouldBeAbsentForNewInstance() {
        Offer offer = new Offer("I offer to repair the roof", LocalDate.now());
        assertThat(offer.getCounterOffer().isPresent()).isFalse();
    }

}
