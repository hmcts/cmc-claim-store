package uk.gov.hmcts.cmc.claimstore.utils;


import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.HashMap;
import java.util.Map;

public class CaseDataContentBuilder {

    // Utility class
    private CaseDataContentBuilder() {

    }

    public static CaseDataContent build(
        StartEventResponse startEventResponse,
        String eventSummary,
        String eventDescription,
        Map<String, Object> contentModified) {

        var payload = new HashMap<>(startEventResponse.getCaseDetails().getData());
        payload.putAll(contentModified);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary(eventSummary)
                .description(eventDescription)
                .build())
            .data(payload)
            .build();
    }
}
