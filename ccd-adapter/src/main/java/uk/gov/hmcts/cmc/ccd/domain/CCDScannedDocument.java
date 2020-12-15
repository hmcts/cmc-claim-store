package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Value
public class CCDScannedDocument {

    private String fileName;
    private String controlNumber;
    private String subtype;
    private String formSubtype;
    private CCDScannedDocumentType type;
    private String submittedBy;
    private LocalDateTime scannedDate;
    private LocalDateTime deliveryDate;
    private CCDDocument url;
    private String exceptionRecordReference;

}
