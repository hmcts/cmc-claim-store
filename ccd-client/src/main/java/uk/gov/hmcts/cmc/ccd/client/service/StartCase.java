package uk.gov.hmcts.cmc.ccd.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cmc.ccd.client.header.HttpHeadersFactory;
import uk.gov.hmcts.cmc.ccd.client.model.EventRequestData;
import uk.gov.hmcts.cmc.ccd.client.model.StartEventResponse;

@Service
public class StartCase {

    private final RestTemplate restTemplate;
    private final HttpHeadersFactory headersFactory;

    @Value("${ccd.caseDataStore.baseUrl}")
    private String ccdUrl;

    @Autowired
    public StartCase(final RestTemplate restTemplate, final HttpHeadersFactory httpHeadersFactory) {
        this.restTemplate = restTemplate;
        this.headersFactory = httpHeadersFactory;
    }

    public StartEventResponse exchange(final EventRequestData eventRequestData) {

        final HttpEntity<String> httpEntity = new HttpEntity<>(headersFactory.getHttpHeader());

        final String startCaseCreationUrl = ccdUrl + "/citizens/" + eventRequestData.getUserId()
            + "/jurisdictions/" + eventRequestData.getJurisdictionId()
            + "/case-types/" + eventRequestData.getCaseTypeId()
            + "/event-triggers/" + eventRequestData.getEventId()
            + "/token"
            + "?ignore-warning=" + eventRequestData.getIgnoreWarning();

        ResponseEntity<StartEventResponse> response = restTemplate.exchange(startCaseCreationUrl, HttpMethod.GET,
            httpEntity, StartEventResponse.class);

        return response.getBody();
    }
}
