package uk.gov.hmcts.cmc.claimstore.requests.courtfinder;

import feign.Feign;
import feign.Request;
import feign.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class CourtFinderApiTest {

    private CourtFinderApi api;
    private MockRestServiceServer tokenServer;
    private final List<Request> requests = new ArrayList<>();

    @Before
    public void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        tokenServer = MockRestServiceServer.bindTo(restTemplate).build();
        tokenServer.expect(requestTo("https://login.microsoftonline.com/tenant/oauth2/v2.0/token"))
            .andRespond(withSuccess("{\"access_token\":\"fact-token\",\"expires_in\":3600}",
                MediaType.APPLICATION_JSON));

        api = Feign.builder()
            .contract(new SpringMvcContract())
            .requestInterceptor(new CourtFinderClientConfiguration()
                .courtFinderAuthentication(restTemplate, "client", "secret", "tenant", "data-api"))
            .client((request, options) -> {
                requests.add(request);
                return Response.builder().request(request).status(200).reason("OK")
                    .headers(Collections.emptyMap()).build();
            })
            .decoder((response, type) -> null)
            .target(CourtFinderApi.class, "https://cft-api-mgmt.platform.hmcts.net/fact");
    }

    @Test
    public void shouldUseVersionedPostcodeEndpointWithBearerToken() {
        api.findMoneyClaimCourtByPostcode("NE1 4LP");

        assertRequest("/search/courts/v1/postcode?postcode=NE1%204LP&serviceArea=money-claims");
    }

    @Test
    public void shouldUseVersionedNameEndpointWithEncodedName() {
        api.findMoneyClaimCourtByName("Newcastle & Gateshead");

        assertRequest("/courts/name/v1?name=Newcastle%20%26%20Gateshead");
    }

    @Test
    public void shouldUseVersionedSlugEndpointWithBearerToken() {
        api.getCourtDetailsFromNameSlug("newcastle-civil-family-courts-and-tribunals-centre");

        assertRequest("/courts/slug/newcastle-civil-family-courts-and-tribunals-centre/v1");
    }

    private void assertRequest(String path) {
        assertThat(requests).hasSize(1);
        Request request = requests.get(0);
        assertThat(request.httpMethod()).isEqualTo(Request.HttpMethod.GET);
        assertThat(request.url()).isEqualTo("https://cft-api-mgmt.platform.hmcts.net/fact" + path);
        assertThat(request.headers().get("Authorization")).containsExactly("Bearer fact-token");
        tokenServer.verify();
    }
}
