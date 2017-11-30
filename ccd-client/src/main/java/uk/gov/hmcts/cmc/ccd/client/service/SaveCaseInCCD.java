package uk.gov.hmcts.cmc.ccd.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.client.exception.InvalidCaseDataException;
import uk.gov.hmcts.cmc.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.cmc.ccd.client.model.CaseDetails;
import uk.gov.hmcts.cmc.ccd.client.model.Event;
import uk.gov.hmcts.cmc.ccd.client.model.EventRequestData;
import uk.gov.hmcts.cmc.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class SaveCaseInCCD {

    private final StartCase startCase;
    private final SubmitCase submitCase;

    @Autowired
    public SaveCaseInCCD(final StartCase startCase, final SubmitCase submitCase) {
        this.startCase = startCase;
        this.submitCase = submitCase;
    }

    public CaseDetails save(final EventRequestData eventRequestData, final CCDCase ccdCase) {

        Map<String, JsonNode> data = toJson(ccdCase);

        StartEventResponse startEventResponse = this.startCase.exchange(eventRequestData);
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("CMC case submission event")
                .description("Submitting CMC case with token " + startEventResponse.getToken())
                .build())
            .data(data)
            .build();
        return this.submitCase.submit(eventRequestData, caseDataContent);
    }

    public Map<String, JsonNode> toJson(CCDCase ccdCase) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode dataNode = objectMapper.readTree(objectMapper.writeValueAsString(ccdCase));
            return objectMapper.convertValue(dataNode, new TypeReference<HashMap<String, JsonNode>>() {
            });

        } catch (IOException e) {
            throw new InvalidCaseDataException(
                String.format("Failed to serialize '%s' to JSON", ccdCase.getClass().getSimpleName()), e
            );
        }
    }

}
