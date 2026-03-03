package uk.gov.hmcts.cmc.claimstore.filters;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.exceptions.ServiceException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Filter to validate inbound Service-to-Service (S2S) authorization tokens.
 * This filter validates the ServiceAuthorization header for protected endpoints.
 */
@Component
public class ServiceAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAuthFilter.class);
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    private final AuthTokenValidator authTokenValidator;
    private final List<String> authorisedServices;

    @Autowired
    public ServiceAuthFilter(
        AuthTokenValidator authTokenValidator,
        @Value("${idam.s2s-auth.authorised-services}") String authorisedServices
    ) {
        this.authTokenValidator = authTokenValidator;
        this.authorisedServices = Arrays.asList(authorisedServices.split(","));
        logger.info("ServiceAuthFilter initialized with authorised services: {}", this.authorisedServices);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/calendar/")
            || path.startsWith("/interest/")
            || path.startsWith("/court-finder/")
            || path.startsWith("/health")
            || path.startsWith("/health/liveness")
            || path.startsWith("/health/readiness")
            || path.startsWith("/swagger-ui.html")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/swagger-resources")
            || path.startsWith("/v2/api-docs")
            || path.startsWith("/status/health");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String serviceAuthHeader = request.getHeader(SERVICE_AUTHORIZATION_HEADER);

        if (StringUtils.isBlank(serviceAuthHeader)) {
            logger.warn("Missing ServiceAuthorization header for {} {}",
                request.getMethod(), request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "Missing ServiceAuthorization header");
            return;
        }

        try {
            // Validate the S2S token and extract service name
            String serviceName = authTokenValidator.getServiceName(serviceAuthHeader);
            logger.debug("Validated S2S token from service: {}", serviceName);

            // Check if service is in allowlist
            if (!authorisedServices.contains(serviceName)) {
                logger.warn("Unauthorized service '{}' attempted to access {} {}",
                    serviceName, request.getMethod(), request.getRequestURI());
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Service '" + serviceName + "' is not authorized");
                return;
            }

            // Store service name in request attribute for use in controllers if needed
            request.setAttribute("s2s-service-name", serviceName);

            // Proceed with the request
            filterChain.doFilter(request, response);

        } catch (InvalidTokenException e) {
            logger.warn("Invalid S2S token for {} {}: {}",
                request.getMethod(), request.getRequestURI(), e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid ServiceAuthorization token");
        } catch (ServiceException e) {
            logger.error("S2S service error for {} {}: {}",
                request.getMethod(), request.getRequestURI(), e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "S2S authorization service error");
        }
    }
}
