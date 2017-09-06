package uk.gov.hmcts.cmc.claimstore.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cmc.claimstore.exceptions.InvalidApplicationException;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class RestClientTest {

    private static final String API_DOMAIN = "http://localhost/";
    private static final String ENDPOINT = "test";
    private static final String BODY = "response body";
    private static final String AUTHORISATION = "Bearer: token";

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private static JsonMapper jsonMapper = new JsonMapper(new ObjectMapper());

    @Before
    public void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void getPassesGivenAuthorisationToken() {
        mockServer
            .expect(requestTo(API_DOMAIN + ENDPOINT))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, AUTHORISATION))
            .andRespond(withSuccess(
                BODY, MediaType.APPLICATION_JSON
            ));

        RestClient restClient = RestClient.Builder.of()
            .restTemplate(restTemplate)
            .apiDomain(API_DOMAIN)
            .jsonMapper(jsonMapper)
            .build();

        String response = restClient.get(ENDPOINT, AUTHORISATION, String.class);
        mockServer.verify();
        assertThat(response, is(BODY));
    }

    @Test
    public void postIsCalledWithDefaultSetup() {
        mockServer
            .expect(requestTo(API_DOMAIN + ENDPOINT))
            .andExpect(method(HttpMethod.POST))
            .andExpect(
                MockRestRequestMatchers.header(HttpHeaders.CONTENT_TYPE, RestClient.Builder.DEFAULT_CONTENT_TYPE)
            )
            .andExpect(header(HttpHeaders.ACCEPT, RestClient.Builder.DEFAULT_ACCEPT))
            .andExpect(header(HttpHeaders.AUTHORIZATION, AUTHORISATION))
            .andRespond(
                withSuccess(BODY, MediaType.APPLICATION_JSON)
            );

        RestClient restClient = RestClient.Builder.of()
            .restTemplate(restTemplate)
            .apiDomain(API_DOMAIN)
            .jsonMapper(jsonMapper)
            .build();

        assertRequestAndResponseAreCorrect(restClient);
    }

    @Test
    public void postIsCalledWithCustomSetup() {
        final String customAuthorisation = "my own authorization";

        mockServer
            .expect(requestTo(API_DOMAIN + ENDPOINT))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
            .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_PDF_VALUE))
            .andExpect(header(HttpHeaders.AUTHORIZATION, customAuthorisation))
            .andRespond(
                withSuccess(BODY, MediaType.APPLICATION_JSON)
            );

        RestClient restClient = RestClient.Builder.of()
            .restTemplate(restTemplate)
            .apiDomain(API_DOMAIN)
            .accept(MediaType.APPLICATION_PDF_VALUE)
            .contentType(MediaType.APPLICATION_PDF_VALUE)
            .jsonMapper(jsonMapper)
            .build();

        assertRequestIsCorrect(restClient, customAuthorisation);
    }

    @Test
    public void postIsCalledTwiceByDifferentUsers() {
        final String customAuthorisation1 = "My name is Bond, James Bond";
        final String customAuthorisation2 = "Luke, I'm your father";

        RestClient restClient = RestClient.Builder.of()
            .restTemplate(restTemplate)
            .apiDomain(API_DOMAIN)
            .jsonMapper(jsonMapper)
            .build();

        mockApiAndMakeCall(restClient, customAuthorisation1);
        mockApiAndMakeCall(restClient, customAuthorisation2);
    }

    private void mockApiAndMakeCall(final RestClient restClient, final String authorisation) {
        mockServer
            .expect(requestTo(API_DOMAIN + ENDPOINT))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, authorisation))
            .andRespond(
                withSuccess(BODY, MediaType.APPLICATION_JSON)
            );

        assertRequestIsCorrect(restClient, authorisation);

        mockServer.reset();
    }

    @Test(expected = InvalidApplicationException.class)
    public void exceptionIsThrownForObjectThatCannotBeMappedToJson() {
        RestClient restClient = RestClient.Builder.of()
            .restTemplate(restTemplate)
            .jsonMapper(jsonMapper)
            .build();

        // instance of Object class cannot be mapped to JSON - it's going to throw exception
        restClient.post("/any-endpoint", new Object(), AUTHORISATION, Object.class);
    }

    @Test
    public void postWithNullBodyWorks() {
        runTestForPostMethodWithDifferentBody(null, "null is allowed as POST request body");
    }

    @Test
    public void postWithEmptyStringBodyWorks() {
        runTestForPostMethodWithDifferentBody(null, "empty is allowed as POST request body");
    }

    @Test
    public void postWithIntBodyWorks() {
        runTestForPostMethodWithDifferentBody(1234, "int is allowed as POST request body");
    }

    private void runTestForPostMethodWithDifferentBody(Object body, String description) {
        mockServer
            .expect(requestTo(API_DOMAIN + ENDPOINT))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withSuccess(BODY, MediaType.APPLICATION_JSON)
            );

        RestClient restClient = RestClient.Builder.of()
            .restTemplate(restTemplate)
            .apiDomain(API_DOMAIN)
            .jsonMapper(jsonMapper)
            .build();

        try {
            assertRequestAndResponseAreCorrect(restClient, body);
        } catch (Exception e) {
            fail(description);
        }
    }

    private void assertRequestAndResponseAreCorrect(RestClient restClient) {
        assertRequestAndResponseAreCorrect(restClient, new HashMap<>());
    }

    private void assertRequestAndResponseAreCorrect(RestClient restClient, Object body) {
        ResponseEntity<String> response = restClient.post(ENDPOINT, body, AUTHORISATION, String.class);
        mockServer.verify();
        assertThat(response.getBody(), is(BODY));
    }

    private void assertRequestIsCorrect(RestClient restClient, String authorisationToken) {
        restClient.post(ENDPOINT, new HashMap<>(), authorisationToken, String.class);
        mockServer.verify();
    }
}
