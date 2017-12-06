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

        JsonNode data = toJson(ccdCase);

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

    private JsonNode toJson(CCDCase ccdCase) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode dataNode = objectMapper.readTree(objectMapper.writeValueAsString(ccdCase));
            return objectMapper.convertValue(dataNode, new TypeReference<JsonNode>() {
            });

        } catch (IOException e) {
            throw new InvalidCaseDataException("Failed to serialize to JSON", e);
        }
    }

}
