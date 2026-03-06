package uk.gov.hmcts.cmc.claimstore.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;

import java.io.IOException;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ServiceAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAuthFilter.class);

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final List<String> WHITELISTED_URLS = List.of(
        "/swagger-ui.html",
        "/webjars/springfox-swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/health",
        "/env",
        "/health/liveness",
        "/health/readiness",
        "/status/health",
        "/",
        "/support/**",
        "/calendar/**",
        "/deadline/**",
        "/interest/**",
        "/court-finder/**",
        "/cases/callbacks/**",
        "/testing-support/**",
        "/user/roles/**",
        "/claims/*/defendant-link-status",
        "/claims/*/metadata",
        "/claims/letter/*",
        "/loggers/**",
        "/claims/**",
        "/responses/**",
        "/documents/**",
        "/scanned-documents"
    );

    private final ServiceAuthorisationApi serviceAuthorisationApi;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ServiceAuthFilter(ServiceAuthorisationApi serviceAuthorisationApi) {
        this.serviceAuthorisationApi = serviceAuthorisationApi;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (isWhitelisted(requestUri)) {
            logger.debug("S2S skipping whitelisted URL: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        String serviceAuthToken = request.getHeader(SERVICE_AUTHORIZATION);

        if (serviceAuthToken == null || serviceAuthToken.isBlank()) {
            logger.error("ServiceAuthorization header is missing for {}", requestUri);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("ServiceAuthorization header is required");
            return;
        }

        try {
            serviceAuthorisationApi.getServiceName(serviceAuthToken);
            filterChain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            logger.error("Invalid S2S token for {}", requestUri, e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Invalid S2S token");
        } catch (Exception e) {
            logger.error("S2S validation failed for {}", requestUri, e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("S2S validation failed");
        }
    }

    private boolean isWhitelisted(String requestUri) {
        return WHITELISTED_URLS.stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }
}
