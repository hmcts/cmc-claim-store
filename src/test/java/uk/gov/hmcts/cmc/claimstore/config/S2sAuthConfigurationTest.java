package uk.gov.hmcts.cmc.claimstore.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.cmc.claimstore.security.S2sAuthFilter;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class S2sAuthConfigurationTest {

    @Mock
    private ServiceAuthorisationApi serviceAuthorisationApi;

    private final S2sAuthConfiguration configuration = new S2sAuthConfiguration();

    @Test
    void shouldCreateFilterWithTrimmedAllowedServices() {
        S2sAuthFilter filter = configuration.s2sAuthFilter(
            serviceAuthorisationApi,
            " service-a, service-b ,,service-c "
        );

        assertThat(ReflectionTestUtils.getField(filter, "allowedServices"))
            .isEqualTo(List.of("service-a", "service-b", "service-c"));
        assertThat(ReflectionTestUtils.getField(filter, "authTokenValidator"))
            .isInstanceOf(ServiceAuthTokenValidator.class);
    }

    @Test
    void shouldCreateFilterWithEmptyAllowedServicesWhenPropertyBlank() {
        S2sAuthFilter filter = configuration.s2sAuthFilter(serviceAuthorisationApi, "  ,  ");

        assertThat(ReflectionTestUtils.getField(filter, "allowedServices"))
            .isEqualTo(List.of());
    }
}
