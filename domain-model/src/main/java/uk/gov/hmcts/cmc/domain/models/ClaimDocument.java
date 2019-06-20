package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.Getter;

import java.net.URI;
import java.time.LocalDateTime;

@Getter
public class ClaimDocument extends CollectionId {
    private final URI documentManagementUrl;
    private final String documentName;
    private final ClaimDocumentType documentType;
    private final LocalDateTime authoredDatetime;
    private final LocalDateTime createdDatetime;
    private final String createdBy;
    private final long size;

    @Builder
    public ClaimDocument(
        String id,
        URI documentManagementUrl,
        String documentName,
        ClaimDocumentType documentType,
        LocalDateTime authoredDatetime,
        LocalDateTime createdDatetime,
        String createdBy,
        long size
    ) {
        super(id);
        this.documentManagementUrl = documentManagementUrl;
        this.documentName = documentName;
        this.documentType = documentType;
        this.authoredDatetime = authoredDatetime;
        this.createdDatetime = createdDatetime;
        this.createdBy = createdBy;
        this.size = size;
    }
}
