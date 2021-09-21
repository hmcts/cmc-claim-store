package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;

import java.net.URI;
import java.time.LocalDateTime;

@Getter
@JsonIgnoreProperties(value = {"documentManagementUrl", "documentManagementBinaryUrl"})
public class ClaimDocument extends CollectionId {
    private final URI documentManagementUrl;
    private final URI documentManagementBinaryUrl;
    private final String documentName;
    private final String documentHash;
    private final ClaimDocumentType documentType;
    private final LocalDateTime authoredDatetime;
    private final LocalDateTime createdDatetime;
    private final LocalDateTime receivedDateTime;
    private final String createdBy;
    private final long size;

    @Builder
    public ClaimDocument(
        String id,
        URI documentManagementUrl,
        URI documentManagementBinaryUrl,
        String documentName,
        String documentHash,
        ClaimDocumentType documentType,
        LocalDateTime authoredDatetime,
        LocalDateTime createdDatetime,
        LocalDateTime receivedDateTime,
        String createdBy,
        long size
    ) {
        super(id);
        this.documentManagementUrl = documentManagementUrl;
        this.documentManagementBinaryUrl = documentManagementBinaryUrl;
        this.documentName = documentName;
        this.documentHash = documentHash;
        this.documentType = documentType;
        this.authoredDatetime = authoredDatetime;
        this.createdDatetime = createdDatetime;
        this.receivedDateTime = receivedDateTime;
        this.createdBy = createdBy;
        this.size = size;
    }
}
