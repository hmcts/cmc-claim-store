package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class CCDClaimDocument {

    private CCDDocument documentLink;
    private String documentName;
    private String documentType;
    private LocalDateTime authoredDatetime;
    private LocalDateTime createdDatetime;
    private String createdBy;
}
