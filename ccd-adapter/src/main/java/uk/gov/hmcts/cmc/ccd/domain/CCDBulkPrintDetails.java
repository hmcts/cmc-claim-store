package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CCDBulkPrintDetails {
    private String printRequestId;
    private CCDPrintRequestType printRequestType;
    private LocalDate printRequestedAt;
}
