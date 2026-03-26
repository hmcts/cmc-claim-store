package uk.gov.hmcts.cmc.claimstore.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.exceptions.ServiceException;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;

import java.io.IOException;
import java.util.List;

public class S2sAuthFilter extends OncePerRequestFilter {
    public static final String SERVICE_AUTH_HEADER = "ServiceAuthorization";
    private static final Logger LOG = LoggerFactory.getLogger(S2sAuthFilter.class);

    private final ServiceAuthTokenValidator authTokenValidator;
    private final List<String> allowedServices;
    private final RequestMatcher protectedEndpoints = new OrRequestMatcher(
        new AntPathRequestMatcher("/support/**"),
        new AntPathRequestMatcher("/cases/callbacks/**"),
        new AntPathRequestMatcher("/testing-support/**"),
        new AntPathRequestMatcher("/loggers/**")
    );

    public S2sAuthFilter(ServiceAuthTokenValidator authTokenValidator, List<String> allowedServices) {
        this.authTokenValidator = authTokenValidator;
        this.allowedServices = allowedServices;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !protectedEndpoints.matches(request);
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (allowedServices.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractBearerToken(request.getHeader(SERVICE_AUTH_HEADER));
        if (token == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        try {
            authTokenValidator.validate(token);
            if (!allowedServices.isEmpty()) {
                String serviceName = authTokenValidator.getServiceName(token);
                if (!allowedServices.contains(serviceName)) {
                    LOG.debug(
                        "service forbidden {} for endpoint: {} method: {}",
                        serviceName,
                        request.getRequestURI(),
                        request.getMethod()
                    );
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    return;
                }
            }
        } catch (InvalidTokenException | ServiceException ex) {
            LOG.warn("Unsuccessful service authentication", ex);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        String bearerPrefix = "Bearer ";
        if (header.startsWith(bearerPrefix)) {
            return header.substring(bearerPrefix.length());
        }
        return header;
    }
}
