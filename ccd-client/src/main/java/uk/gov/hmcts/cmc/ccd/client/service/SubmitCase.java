package uk.gov.hmcts.cmc.ccd.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cmc.ccd.client.header.HttpHeadersFactory;
import uk.gov.hmcts.cmc.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.cmc.ccd.client.model.CaseDetails;
import uk.gov.hmcts.cmc.ccd.client.model.EventRequestData;

@Service
public class SubmitCase {

    private final RestTemplate restTemplate;
    private final HttpHeadersFactory headersFactory;

    @Value("${ccd.caseDataStore.baseUrl}")
    private String ccdUrl;

    @Autowired
    public SubmitCase(final RestTemplate restTemplate, final HttpHeadersFactory httpHeadersFactory) {
        this.restTemplate = restTemplate;
        this.headersFactory = httpHeadersFactory;
    }

    public CaseDetails submit(final EventRequestData eventRequestData, final CaseDataContent caseDataContent) {

        final HttpEntity<?> httpEntity = new HttpEntity<Object>(caseDataContent, headersFactory.getHttpHeader());

        final String submitCaseUrl = ccdUrl
            + "/citizens/" + eventRequestData.getUserId()
            + "/jurisdictions/" + eventRequestData.getJurisdictionId()
            + "/case-types/" + eventRequestData.getCaseTypeId()
            + "/cases"
            + "?ignore-warning=" + eventRequestData.getIgnoreWarning();

        ResponseEntity<CaseDetails> response = restTemplate.exchange(
            submitCaseUrl, HttpMethod.POST, httpEntity, CaseDetails.class);

        return response.getBody();
    }
}
