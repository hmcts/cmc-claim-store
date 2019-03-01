package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.net.URI;
import java.time.LocalDateTime;

@Value
@Builder
public class CCDClaimDocument {

    private URI documentLink;
    private String documentName;
    private CCDClaimDocumentType documentType;
    private LocalDateTime authoredDatetime;
    private LocalDateTime createdDatetime;
    private String createdBy;
}
