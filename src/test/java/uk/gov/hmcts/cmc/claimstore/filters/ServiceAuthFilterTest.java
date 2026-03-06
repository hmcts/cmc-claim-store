package uk.gov.hmcts.cmc.claimstore.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.exceptions.ServiceException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.io.IOException;
import javax.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceAuthFilterTest {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String AUTHORISED_SERVICES = "cmc,ccd_gw";
    private static final String AUTHORISED_SERVICE = "cmc";
    private static final String UNAUTHORISED_SERVICE = "unknown";
    private static final String S2S_TOKEN = "Bearer token";

    @Mock
    private AuthTokenValidator authTokenValidator;

    private ServiceAuthFilter serviceAuthFilter;

    @BeforeEach
    void setUp() {
        serviceAuthFilter = new ServiceAuthFilter(authTokenValidator, AUTHORISED_SERVICES);
    }

    @Test
    void shouldNotFilterExcludedPaths() {
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/health")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/health/liveness")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/health/readiness")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/calendar/")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/interest/")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/court-finder/")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/swagger-ui.html")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/swagger-ui/")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/swagger-resources")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/v2/api-docs")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/v3/api-docs")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/status/health")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/deadline/")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/cases/callbacks/")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/testing-support/")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/claims/letter/")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/claims/123/defendant-link-status")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/loggers/")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/env")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/")));
        assertTrue(serviceAuthFilter.shouldNotFilter(createRequest("/user/roles")));
    }

    @Test
    void shouldFilterProtectedPaths() {
        assertFalse(serviceAuthFilter.shouldNotFilter(createRequest("/claims")));
        assertFalse(serviceAuthFilter.shouldNotFilter(createRequest("/claims/123")));
        assertFalse(serviceAuthFilter.shouldNotFilter(createRequest("/responses")));
    }

    @Test
    void shouldReturnUnauthorizedWhenHeaderIsMissing() throws ServletException, IOException {
        MockHttpServletRequest request = createRequest("/claims");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(MockHttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Missing ServiceAuthorization header", response.getErrorMessage());
        verifyNoInteractions(authTokenValidator);
    }

    @Test
    void shouldReturnForbiddenWhenServiceIsNotAuthorised() throws ServletException, IOException {
        MockHttpServletRequest request = createRequest("/claims");
        request.addHeader(SERVICE_AUTHORIZATION_HEADER, S2S_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(authTokenValidator.getServiceName(S2S_TOKEN)).thenReturn(UNAUTHORISED_SERVICE);

        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(MockHttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals("Service 'unknown' is not authorized", response.getErrorMessage());
    }

    @Test
    void shouldProceedWhenServiceIsAuthorised() throws ServletException, IOException {
        MockHttpServletRequest request = createRequest("/claims");
        request.addHeader(SERVICE_AUTHORIZATION_HEADER, S2S_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(authTokenValidator.getServiceName(S2S_TOKEN)).thenReturn(AUTHORISED_SERVICE);

        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(MockHttpServletResponse.SC_OK, response.getStatus());
        assertEquals(AUTHORISED_SERVICE, request.getAttribute("s2s-service-name"));
        // Check if filterChain.doFilter was called by checking if any further processing would happen
        // In MockFilterChain, we can check if the request was passed through
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() throws ServletException, IOException {
        MockHttpServletRequest request = createRequest("/claims");
        request.addHeader(SERVICE_AUTHORIZATION_HEADER, S2S_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(authTokenValidator.getServiceName(S2S_TOKEN)).thenThrow(new InvalidTokenException("Invalid token"));

        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(MockHttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Invalid ServiceAuthorization token", response.getErrorMessage());
    }

    @Test
    void shouldReturnInternalServerErrorWhenServiceFails() throws ServletException, IOException {
        MockHttpServletRequest request = createRequest("/claims");
        request.addHeader(SERVICE_AUTHORIZATION_HEADER, S2S_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(authTokenValidator.getServiceName(S2S_TOKEN)).thenThrow(new ServiceException("Service error", new RuntimeException()));

        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(MockHttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("S2S authorization service error", response.getErrorMessage());
    }

    private MockHttpServletRequest createRequest(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }
}
