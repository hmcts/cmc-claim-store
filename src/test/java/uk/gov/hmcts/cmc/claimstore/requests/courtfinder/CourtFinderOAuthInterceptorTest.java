package uk.gov.hmcts.cmc.claimstore.requests.courtfinder;

import feign.RequestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

public class CourtFinderOAuthInterceptorTest {

    private MockRestServiceServer server;
    private CourtFinderOAuthInterceptor interceptor;
    private Clock clock;

    @Before
    public void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        clock = mock(Clock.class);
        when(clock.instant()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        interceptor = new CourtFinderOAuthInterceptor(restTemplate, "client", "secret&+", "tenant", "data-api", clock);
    }

    @Test
    public void shouldRequestAndReuseBearerToken() {
        server.expect(requestTo("https://login.microsoftonline.com/tenant/oauth2/v2.0/token"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(content().string("client_id=client&client_secret=secret%26%2B"
                + "&grant_type=client_credentials&scope=api%3A%2F%2Fdata-api%2F.default"))
            .andRespond(withSuccess("{\"access_token\":\"token\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));

        RequestTemplate first = new RequestTemplate();
        first.header("Authorization", "Bearer old-token");
        interceptor.apply(first);
        RequestTemplate second = new RequestTemplate();
        interceptor.apply(second);

        assertThat(first.headers().get("Authorization")).containsExactly("Bearer token");
        assertThat(second.headers().get("Authorization")).containsExactly("Bearer token");
        server.verify();
    }

    @Test
    public void shouldRefreshBeforeTokenExpires() {
        server.expect(requestTo("https://login.microsoftonline.com/tenant/oauth2/v2.0/token"))
            .andRespond(withSuccess("{\"access_token\":\"first\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://login.microsoftonline.com/tenant/oauth2/v2.0/token"))
            .andRespond(withSuccess("{\"access_token\":\"second\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));

        interceptor.apply(new RequestTemplate());
        when(clock.instant()).thenReturn(Instant.parse("2026-01-01T00:59:00Z"));
        RequestTemplate request = new RequestTemplate();
        interceptor.apply(request);

        assertThat(request.headers().get("Authorization")).containsExactly("Bearer second");
        server.verify();
    }

    @Test
    public void shouldNotSendRequestWhenTokenAcquisitionFails() {
        server.expect(requestTo("https://login.microsoftonline.com/tenant/oauth2/v2.0/token"))
            .andRespond(withUnauthorizedRequest());

        RequestTemplate request = new RequestTemplate();
        assertThatThrownBy(() -> interceptor.apply(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Unable to obtain CourtFinder OAuth token");
        server.verify();
    }

    @Test
    public void shouldRejectMissingToken() {
        server.expect(requestTo("https://login.microsoftonline.com/tenant/oauth2/v2.0/token"))
            .andRespond(withSuccess("{\"expires_in\":3600}", MediaType.APPLICATION_JSON));

        RequestTemplate request = new RequestTemplate();
        assertThatThrownBy(() -> interceptor.apply(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Invalid CourtFinder OAuth token response");
        server.verify();
    }
}
