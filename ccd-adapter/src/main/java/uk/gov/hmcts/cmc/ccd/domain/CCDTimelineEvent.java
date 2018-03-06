package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDTimelineEvent {

    private String date;
    private String description;
    
}
