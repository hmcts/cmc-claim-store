package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class CCDClaimDocument {

    private CCDDocument documentLink;
    private String documentName;
    private CCDClaimDocumentType documentType;
    private LocalDateTime authoredDatetime;
    private LocalDateTime createdDatetime;
    private LocalDateTime receivedDatetime;
    private String createdBy;
    private long size;
}
