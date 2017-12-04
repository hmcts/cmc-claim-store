package uk.gov.hmcts.cmc.ccd.client.model;

import lombok.Data;
import lombok.Value;

@Data
@Value
public class EventRequestData {
    private String userToken;
    private String userId;
    private String jurisdictionId;
    private String caseTypeId;
    private String eventId;
    private Boolean ignoreWarning;
}
