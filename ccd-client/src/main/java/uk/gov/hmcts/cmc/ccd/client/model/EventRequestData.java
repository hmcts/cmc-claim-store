package uk.gov.hmcts.cmc.ccd.client.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventRequestData {
    private String userToken;
    private String userId;
    private String jurisdictionId;
    private String caseTypeId;
    private String eventId;
    private boolean ignoreWarning;
}
