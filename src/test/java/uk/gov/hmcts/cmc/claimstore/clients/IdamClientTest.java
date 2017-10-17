package uk.gov.hmcts.cmc.claimstore.clients;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cmc.claimstore.idam.client.IdamClient;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinRequest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.processors.RestClientFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdamClientTest {

    private static final String IDAM_API_URL = "idam-api-url";
    private static final String PIN_ENDPOINT = "/pin";
    private static final String PIN = "my-pin-generated";
    private static final String LETTER_HOLDER_ID = "1";
    private static final String AUTHORISATION = "Bearer: token";
    private static final GeneratePinRequest request = new GeneratePinRequest("a b");
    private static final GeneratePinResponse expResponseBody = new GeneratePinResponse(PIN, LETTER_HOLDER_ID);

    @Mock
    private RestClientFactory restClientFactory;
    @Mock
    private RestClient restClient;

    private IdamClient idamClient;

    @Before
    public void setUp() {
        when(restClientFactory.create(eq(IDAM_API_URL))).thenReturn(restClient);
        idamClient = new IdamClient(restClientFactory, IDAM_API_URL);
    }

    @Test
    public void generatePinCallsIdamPinEndpoint() {
        when(
            restClient
                .post(
                    eq(PIN_ENDPOINT), any(GeneratePinRequest.class), eq(AUTHORISATION), eq(GeneratePinResponse.class))
        ).thenReturn(new ResponseEntity<>(expResponseBody, HttpStatus.OK));

        GeneratePinResponse response = idamClient.generatePin(request, AUTHORISATION);

        assertThat(response.getPin()).isEqualTo(expResponseBody.getPin());
        assertThat(response.getUserId()).isEqualTo(expResponseBody.getUserId());
    }

    @Test
    public void retrieveUserDetailsShouldSendAuthHeaderToDetailsEndpoint() {
        String authHeader = "secureAuthHeader";
        idamClient.retrieveUserDetails(authHeader);
        verify(restClient).get(eq(IdamClient.Endpoints.DETAILS), eq(authHeader), eq(UserDetails.class));
    }

}
