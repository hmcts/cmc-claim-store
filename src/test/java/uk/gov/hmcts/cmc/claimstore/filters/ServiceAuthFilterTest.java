package uk.gov.hmcts.cmc.claimstore.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceAuthFilterTest {

    @Mock
    private ServiceAuthorisationApi serviceAuthorisationApi;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    private ServiceAuthFilter serviceAuthFilter;

    @BeforeEach
    void setUp() {
        serviceAuthFilter = new ServiceAuthFilter(serviceAuthorisationApi);
    }

    @Test
    void shouldAllowWhitelistedUrl() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/health");

        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(serviceAuthorisationApi, never()).getServiceName(anyString());
    }

    @Test
    void shouldAllowWhitelistedUrlWithWildcard() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/support/re-send-mediation");

        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(serviceAuthorisationApi, never()).getServiceName(anyString());
    }

    @Test
    void shouldForbiddenIfHeaderMissingForNonWhitelistedUrl() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/some/private/url");
        when(request.getHeader(ServiceAuthFilter.SERVICE_AUTHORIZATION)).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldAllowIfTokenIsValid() throws ServletException, IOException {
        String token = "valid-token";
        when(request.getRequestURI()).thenReturn("/some/private/url");
        when(request.getHeader(ServiceAuthFilter.SERVICE_AUTHORIZATION)).thenReturn(token);
        when(serviceAuthorisationApi.getServiceName(token)).thenReturn("some-service");

        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldForbiddenIfTokenIsInvalid() throws ServletException, IOException {
        String token = "invalid-token";
        when(request.getRequestURI()).thenReturn("/some/private/url");
        when(request.getHeader(ServiceAuthFilter.SERVICE_AUTHORIZATION)).thenReturn(token);
        when(serviceAuthorisationApi.getServiceName(token)).thenThrow(new InvalidTokenException("Invalid token"));
        when(response.getWriter()).thenReturn(writer);

        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(filterChain, never()).doFilter(request, response);
    }
}
