package uk.gov.hmcts.cmc.claimstore.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S2sAuthFilterTest {

    @Mock
    private ServiceAuthTokenValidator authTokenValidator;

    @Mock
    private FilterChain filterChain;

    @Test
    void shouldNotFilterUnprotectedEndpoints() {
        S2sAuthFilter filter = new S2sAuthFilter(authTokenValidator, List.of("cmc"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/claims/123");
        request.setServletPath("/claims/123");

        boolean shouldNotFilter = filter.shouldNotFilter(request);

        assertTrue(shouldNotFilter);
    }

    @Test
    void shouldFilterProtectedEndpoints() {
        S2sAuthFilter filter = new S2sAuthFilter(authTokenValidator, List.of("cmc"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/support/ping");
        request.setServletPath("/support/ping");

        boolean shouldNotFilter = filter.shouldNotFilter(request);

        assertFalse(shouldNotFilter);
    }

    @Test
    void shouldPassThroughWhenAllowedServicesIsEmpty() throws ServletException, IOException {
        S2sAuthFilter filter = new S2sAuthFilter(authTokenValidator, Collections.emptyList());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/support/ping");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authTokenValidator);
    }

    @Test
    void shouldReturnUnauthorizedWhenHeaderMissing() throws ServletException, IOException {
        S2sAuthFilter filter = new S2sAuthFilter(authTokenValidator, List.of("cmc"));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/support/ping");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenInvalid() throws ServletException, IOException {
        S2sAuthFilter filter = new S2sAuthFilter(authTokenValidator, List.of("cmc"));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/support/ping");
        request.addHeader(S2sAuthFilter.SERVICE_AUTH_HEADER, "Bearer test-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doThrow(new InvalidTokenException("invalid token")).when(authTokenValidator).validate("test-token");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldReturnForbiddenWhenServiceNotAllowed() throws ServletException, IOException {
        S2sAuthFilter filter = new S2sAuthFilter(authTokenValidator, List.of("cmc"));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/support/ping");
        request.addHeader(S2sAuthFilter.SERVICE_AUTH_HEADER, "Bearer test-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authTokenValidator.getServiceName("test-token")).thenReturn("bulk_scan");

        filter.doFilterInternal(request, response, filterChain);

        verify(authTokenValidator).validate("test-token");
        assertEquals(403, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldPassThroughWhenServiceIsAllowed() throws ServletException, IOException {
        S2sAuthFilter filter = new S2sAuthFilter(authTokenValidator, List.of("cmc"));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/support/ping");
        request.addHeader(S2sAuthFilter.SERVICE_AUTH_HEADER, "Bearer test-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authTokenValidator.getServiceName("test-token")).thenReturn("cmc");

        filter.doFilterInternal(request, response, filterChain);

        verify(authTokenValidator).validate("test-token");
        verify(authTokenValidator).getServiceName("test-token");
        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldHandleTokenWithoutBearerPrefix() throws ServletException, IOException {
        S2sAuthFilter filter = new S2sAuthFilter(authTokenValidator, List.of("cmc"));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/support/ping");
        request.addHeader(S2sAuthFilter.SERVICE_AUTH_HEADER, "plain-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authTokenValidator.getServiceName("plain-token")).thenReturn("cmc");

        filter.doFilterInternal(request, response, filterChain);

        verify(authTokenValidator).validate("plain-token");
        verify(authTokenValidator).getServiceName("plain-token");
        verify(filterChain).doFilter(request, response);
    }
}
