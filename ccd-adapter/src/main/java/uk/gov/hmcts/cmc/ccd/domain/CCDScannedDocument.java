package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Builder
@Value
public class CCDScannedDocument {

    private String fileName;
    private String controlNumber;
    private CCDScannedDocumentType documentType;
    private String subtype;
    private LocalDateTime scannedDate;
    private LocalDateTime deliveryDate;
    private CCDDocument url;
    private String exceptionRecordReference;

}
