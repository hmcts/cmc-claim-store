package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Value
public class CCDTransferContent {

    private LocalDate dateOfTransfer;
    private String transferCourtName;
    private CCDAddress transferCourtAddress;
    private CCDTransferReason transferReason;
    private String transferReasonOther;
}
