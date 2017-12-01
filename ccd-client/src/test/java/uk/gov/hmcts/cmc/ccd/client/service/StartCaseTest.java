package uk.gov.hmcts.cmc.ccd.client.service;

import com.sun.tools.javadoc.Start;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cmc.ccd.client.header.HttpHeadersFactory;
import uk.gov.hmcts.cmc.ccd.client.model.EventRequestData;
import uk.gov.hmcts.cmc.ccd.client.model.StartEventResponse;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

@RunWith(MockitoJUnitRunner.class)
public class StartCaseTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private HttpHeadersFactory headersFactory;

    private StartCase startCase;

    private EventRequestData eventRequestData = new EventRequestData();

    @Mock
    private HttpHeaders httpHeaders;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExchangeReturnsStartEvent() throws Exception {
        when(headersFactory.getHttpHeader()).thenReturn(httpHeaders);
        ResponseEntity<StartEventResponse> responseEntity = Mockito.mock(ResponseEntity.class);
        HttpEntity<String> httpEntity = Mockito.mock(HttpEntity.class);

        when(restTemplate.exchange(Mockito.anyString(),
            eq(HttpMethod.GET),
            eq(httpEntity),
            eq(StartEventResponse.class) )).thenReturn(responseEntity);

        StartEventResponse startEventResponse = startCase.exchange(eventRequestData);

        Assert.assertNotNull(startEventResponse);

        Mockito.verify(headersFactory).getHttpHeader();
        Mockito.verify(restTemplate).exchange(Mockito.anyString(),
            eq(HttpMethod.GET),
            eq(httpEntity),
            eq(StartEventResponse.class));
    }

    @Test(expected = RestClientException.class)
    public void testExchangeReturnsException() throws Exception {
        when(headersFactory.getHttpHeader()).thenReturn(httpHeaders);
        HttpEntity<String> httpEntity = Mockito.mock(HttpEntity.class);

        when(restTemplate.exchange(Mockito.anyString(),
            eq(HttpMethod.GET),
            eq(httpEntity),
            eq(StartEventResponse.class) )).thenThrow(RestClientException.class);

        StartEventResponse startEventResponse = startCase.exchange(eventRequestData);

        Mockito.verify(headersFactory).getHttpHeader();
        Mockito.verify(restTemplate).exchange(Mockito.anyString(),
            eq(HttpMethod.GET),
            eq(httpEntity),
            eq(StartEventResponse.class));
    }
}
