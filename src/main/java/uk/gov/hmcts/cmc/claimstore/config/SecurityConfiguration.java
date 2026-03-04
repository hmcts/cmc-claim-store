package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import uk.gov.hmcts.cmc.claimstore.filters.ServiceAuthFilter;
import uk.gov.hmcts.cmc.claimstore.security.JwtGrantedAuthoritiesConverter;

import javax.inject.Inject;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String[] AUTHORITIES = {
        "citizen",
        "solicitor",
        "letter-holder",
        "caseworker"
    };

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Value("${oidc.issuer}")
    private String issuerOverride;

    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final ServiceAuthFilter serviceAuthFilter;

    @Inject
    public SecurityConfiguration(
        final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter,
        final ServiceAuthFilter serviceAuthFilter
    ) {
        this.jwtAuthenticationConverter = new JwtAuthenticationConverter();
        this.jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        this.serviceAuthFilter = serviceAuthFilter;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/springfox-swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/health",
            "/env",
            "/health/liveness",
            "/health/readiness",
            "/status/health",
            "/",
            // Public utility endpoints (no sensitive data)
            "/calendar/**",
            "/interest/**",
            "/court-finder/**"
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Add S2S validation filter BEFORE authentication
            .addFilterBefore(serviceAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement().sessionCreationPolicy(STATELESS).and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .authorizeRequests()
            // S2S-only endpoints (internal microservice-to-microservice)
            .antMatchers("/cases/callbacks/**").authenticated()        // S2S only
            .antMatchers("/support/**").authenticated()                // S2S only
            .antMatchers("/testing-support/**").authenticated()        // S2S only (if enabled)
            .antMatchers("/claims/*/metadata").authenticated()         // S2S or User
            .antMatchers("/claims/letter/*").authenticated()           // S2S or User
            // User + S2S endpoints (require both user token and S2S)
            .antMatchers("/claims/**", "/responses/**", "/documents/**")
            .hasAnyAuthority(AUTHORITIES)
            // Metadata and role endpoints
            .antMatchers("/user/roles/**").authenticated()
            .anyRequest()
            .authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(jwtAuthenticationConverter)
            .and()
            .and()
            .oauth2Client();
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuerUri);

        // We are using issuerOverride instead of issuerUri as SIDAM has the wrong issuer at the moment
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        OAuth2TokenValidator<Jwt> withIssuer = new JwtIssuerValidator(issuerOverride);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withTimestamp, withIssuer);

        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }
}
