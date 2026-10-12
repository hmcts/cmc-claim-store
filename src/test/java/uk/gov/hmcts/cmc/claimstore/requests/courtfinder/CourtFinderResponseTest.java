package uk.gov.hmcts.cmc.claimstore.requests.courtfinder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.name.SearchCourtByNameResponse;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.postcode.CourtDetails;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.slug.SearchCourtBySlugResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CourtFinderResponseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldDecodePostcodeSearchArray() throws Exception {
        List<CourtDetails> courts = mapper.readValue("""
            [{"courtId":"00000000-0000-0000-0000-000000000001",
              "courtName":"Example Court","courtSlug":"example-court","distance":1.25}]
            """, new TypeReference<>() { });

        assertThat(courts).hasSize(1);
        assertThat(courts.getFirst().getName()).isEqualTo("Example Court");
        assertThat(courts.getFirst().getSlug()).isEqualTo("example-court");
        assertThat(courts.getFirst().getDistance()).isEqualTo(1.25);
        assertThat(courts.getFirst().getCourtId()).isNotNull();
    }

    @Test
    void shouldDecodeSingleCourtByName() throws Exception {
        SearchCourtByNameResponse court = mapper.readValue("""
            {"id":"00000000-0000-0000-0000-000000000001","name":"Example Court",
             "slug":"example-court","open":true,"lastUpdatedAt":"2026-10-12T12:00:00Z"}
            """, SearchCourtByNameResponse.class);

        assertThat(court.getName()).isEqualTo("Example Court");
        assertThat(court.getSlug()).isEqualTo("example-court");
        assertThat(court.getOpen()).isTrue();
        assertThat(court.getLastUpdatedAt()).isEqualTo("2026-10-12T12:00:00Z");
    }

    @Test
    void shouldMapSlugDetailsToClaimStoreCourtFields() throws Exception {
        SearchCourtBySlugResponse court = mapper.readValue("""
            {"name":"Example Court","slug":"example-court",
             "courtAddresses":[{"addressLine1":"1 High Street","addressLine2":null,
                "townCity":"Hull","postcode":"HU1 2EZ","addressType":"WRITE_TO_US"}],
             "courtAreasOfLaw":[{"areasOfLaw":[{"name":"Money claims",
                "displayName":"Money claims","externalLink":"https://example.com"}]}],
             "courtFacilities":[{"parking":true,"wifi":true,"quietRoom":false}]}
            """, SearchCourtBySlugResponse.class);

        assertThat(court.getAddresses()).hasSize(1);
        assertThat(court.getAddresses().getFirst().getAddressLines()).containsExactly("1 High Street");
        assertThat(court.getAddresses().getFirst().getTown()).isEqualTo("Hull");
        assertThat(court.getAddresses().getFirst().getPostcode()).isEqualTo("HU1 2EZ");
        assertThat(court.getAddresses().getFirst().getType()).isEqualTo("Write to us");
        assertThat(court.getAreasOfLaw()).extracting("name").containsExactly("Money claims");
        assertThat(court.getFacilities()).extracting("name").containsExactly("Parking", "WiFi");
    }
}
