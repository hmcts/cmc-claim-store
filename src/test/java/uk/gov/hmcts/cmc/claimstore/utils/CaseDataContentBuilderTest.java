package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.cmc.claimstore.utils.CaseDataContentBuilder.build;

class CaseDataContentBuilderTest {

    @Test
    public void shouldReturnUpdatedCaseDetails() {

        StartEventResponse eventResponse = StartEventResponse.builder()
            .token("Some token")
            .eventId("Event")
            .caseDetails(CaseDetails.builder()
                .id(123l)
                .state("open")
                .data(Map.of("name", "My Name"))
                .build())
            .build();
        Map<String, Object> newData = Map.of("gender", "male");

        var result = build(eventResponse, "Event", "Description", newData);

        assertEquals("Event", result.getEvent().getId());
        assertEquals("Some token", result.getEventToken());
        assertEquals(Map.of("name", "My Name", "gender", "male"), result.getData());
    }
}
