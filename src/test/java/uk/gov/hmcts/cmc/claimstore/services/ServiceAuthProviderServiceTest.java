package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAuthProviderServiceTest {
    private static final String BEARER_TOKEN = "bearer Token";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    private ServiceAuthProviderService serviceAuthProviderService;

    @Before
    public void setup() {
        serviceAuthProviderService = new ServiceAuthProviderService(authTokenGenerator);
    }

    @Test
    public void shouldGetServiceToken() {
        //given
        when(authTokenGenerator.generate()).thenReturn(BEARER_TOKEN);

        //when
        String output = serviceAuthProviderService.getToken();

        //then
        assertThat(output).isNotNull().isEqualTo(BEARER_TOKEN);

        //verify
        verify(authTokenGenerator).generate();
    }

}
