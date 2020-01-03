package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.net.URI;
import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode(callSuper = true)
@Value
public class ScannedDocument extends CollectionId {
    private final String fileName;
    private final ScannedDocumentType documentType;
    private final String controlNumber;
    private final String subtype;
    private final LocalDateTime scannedDate;
    private final LocalDateTime deliveryDate;
    private final String exceptionRecordReference;
    private final URI documentManagementUrl;
    private final URI documentManagementBinaryUrl;

    @Builder
    public ScannedDocument(
        String id,
        String fileName,
        ScannedDocumentType documentType,
        String controlNumber,
        String subtype,
        LocalDateTime scannedDate,
        LocalDateTime deliveryDate,
        String exceptionRecordReference,
        URI documentManagementUrl,
        URI documentManagementBinaryUrl
    ) {
        super(id);
        this.fileName = fileName;
        this.documentType = documentType;
        this.controlNumber = controlNumber;
        this.exceptionRecordReference = exceptionRecordReference;
        this.subtype = subtype;
        this.scannedDate = scannedDate;
        this.deliveryDate = deliveryDate;
        this.documentManagementUrl = documentManagementUrl;
        this.documentManagementBinaryUrl = documentManagementBinaryUrl;
    }
}
