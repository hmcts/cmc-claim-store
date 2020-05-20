package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Builder
@Value
public class CCDTransferContent {

    private LocalDate dateOfTransfer;
    private String caseworkerName;
    private String reasonForTransfer;
    private String nameOfTransferCourt;
    private CCDAddress addressOfTransferCourt;
}
