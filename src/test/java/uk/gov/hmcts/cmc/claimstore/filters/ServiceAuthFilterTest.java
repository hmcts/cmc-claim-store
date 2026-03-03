package uk.gov.hmcts.cmc.claimstore.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceAuthFilterTest {

    @Mock
    private AuthTokenValidator authTokenValidator;

    @Mock
    private FilterChain filterChain;

    private ServiceAuthFilter serviceAuthFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        serviceAuthFilter = new ServiceAuthFilter(authTokenValidator, "ccd_data,cmc_claim_store");
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldAllowRequestWithValidS2SToken() throws Exception {
        // Given
        request.addHeader("ServiceAuthorization", "Bearer valid-token");
        when(authTokenValidator.getServiceName("Bearer valid-token")).thenReturn("ccd_data");

        // When
        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void shouldRejectRequestWithMissingS2SToken() throws Exception {
        // When
        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void shouldRejectRequestWithInvalidS2SToken() throws Exception {
        // Given
        request.addHeader("ServiceAuthorization", "Bearer invalid-token");
        when(authTokenValidator.getServiceName(anyString())).thenThrow(new InvalidTokenException("Invalid token"));

        // When
        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void shouldRejectRequestFromUnauthorizedService() throws Exception {
        // Given
        request.addHeader("ServiceAuthorization", "Bearer valid-token");
        when(authTokenValidator.getServiceName("Bearer valid-token")).thenReturn("unauthorized_service");

        // When
        serviceAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void shouldNotFilterPublicEndpoints() throws Exception {
        String[] publicEndpoints = {"/calendar/something", "/interest/calculate", "/court-finder/search"};

        for (String endpoint : publicEndpoints) {
            MockHttpServletRequest publicRequest = new MockHttpServletRequest();
            publicRequest.setRequestURI(endpoint);
            MockHttpServletResponse publicResponse = new MockHttpServletResponse();
            FilterChain mockFilterChain = mock(FilterChain.class);

            serviceAuthFilter.doFilter(publicRequest, publicResponse, mockFilterChain);

            verify(mockFilterChain).doFilter(publicRequest, publicResponse);
            assertThat(publicResponse.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        }
    }
}
