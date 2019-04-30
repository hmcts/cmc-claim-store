package uk.gov.hmcts.cmc.claimstore.courtfinder;

import com.github.tomakehurst.wiremock.WireMockServer;
import feign.FeignException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.CourtDetails;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWireMock(port = 0)
@TestPropertySource(value = "/environment.properties", properties = {
    "courtfinder.api.url=http://localhost:${wiremock.server.port}"
})
public class CourtFinderApiTest {

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private CourtFinderApi courtFinderApi;

    @Before
    public void setupMocks() {
        wireMockServer.stubFor(get(urlEqualTo("/search/results.json?postcode=A111AA&spoe=nearest&aol=Money+Claims"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(readPostcodeJSON())
            )
        );

        wireMockServer.stubFor(get(urlEqualTo("/courts/sample-court.json"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(readCourtJson())
                )
            );
    }

    @Test
    public void shouldFindPostcodeThatExists() {

       List<Court> court = courtFinderApi.findMoneyClaimCourtByPostcode("A111AA");

       assertThat(!court.isEmpty());
       assertThat(court.get(0).getName().equals("Dudley County Court and Family Court"));
    }

    @Test(expected = FeignException.class)
    public void shouldThrowExceptionWhenPostcodeNotFound() {
        courtFinderApi.findMoneyClaimCourtByPostcode("A111AB");
    }

    @Test
    public void shouldFindCourtThatExists() {
        CourtDetails details = courtFinderApi.getCourtDetailsFromNameSlug("sample-court");

        assertThat(details != null);
        assertThat(details.getName().equals("Dudley County Court and Family Court"));
    }

    @Test(expected = FeignException.class)
    public void shouldThrowExceptionWhenCourtNotFound() {
        courtFinderApi.getCourtDetailsFromNameSlug("not-exist");
    }

    private static String readPostcodeJSON() {
        return new ResourceReader().read("/courtfinder-postcode-search.json");
    }

    private static String readCourtJson() {
        return new ResourceReader().read("/courtfinder-court-search.json");
    }

}
