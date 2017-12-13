package uk.gov.hmcts.cmc.claimstore.services.ccd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.client.StartForCaseworkerApi;
import uk.gov.hmcts.cmc.ccd.client.SubmitForCaseworkerApi;
import uk.gov.hmcts.cmc.ccd.client.exception.InvalidCaseDataException;
import uk.gov.hmcts.cmc.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.cmc.ccd.client.model.CaseDetails;
import uk.gov.hmcts.cmc.ccd.client.model.Event;
import uk.gov.hmcts.cmc.ccd.client.model.EventRequestData;
import uk.gov.hmcts.cmc.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "core_case_data", havingValue = "true")
public class SaveCoreCaseDataService {

    private final StartForCaseworkerApi startForCaseworkerApi;
    private final SubmitForCaseworkerApi submitForCaseworkerApi;
    private final ObjectMapper objectMapper;
    private final AuthTokenGenerator authTokenGenerator;


    @Autowired
    public SaveCoreCaseDataService(
        final StartForCaseworkerApi startForCaseworkerApi,
        final SubmitForCaseworkerApi submitForCaseworkerApi,
        final ObjectMapper objectMapper,
        final AuthTokenGenerator authTokenGenerator
    ) {
        this.startForCaseworkerApi = startForCaseworkerApi;
        this.submitForCaseworkerApi = submitForCaseworkerApi;
        this.objectMapper = objectMapper;
        this.authTokenGenerator = authTokenGenerator;
    }

    public CaseDetails save(
        final String authorisation,
        final EventRequestData eventRequestData,
        final CCDCase ccdCase
    ) {

        ResponseEntity<StartEventResponse> responseEntity = this.startForCaseworkerApi.start(
            authorisation,
            this.authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.getEventId()
        );

        StartEventResponse startEventResponse = responseEntity.getBody();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("CMC case submission event")
                .description("Submitting CMC case with token " + startEventResponse.getToken())
                .build())
            .data(toJson(ccdCase))
            .build();

        return this.submitForCaseworkerApi.submit(
            authorisation,
            this.authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.isIgnoreWarning(),
            caseDataContent
        ).getBody();
    }

    private JsonNode toJson(CCDCase ccdCase) {
        try {
            JsonNode dataNode = objectMapper.readTree(objectMapper.writeValueAsString(ccdCase));
            return objectMapper.convertValue(dataNode, JsonNode.class);
        } catch (IOException e) {
            throw new InvalidCaseDataException("Failed to serialize to JSON", e);
        }
    }

}
