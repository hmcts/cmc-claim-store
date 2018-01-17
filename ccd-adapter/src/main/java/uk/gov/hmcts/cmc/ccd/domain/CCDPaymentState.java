package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDPaymentState {
    private String status;
    private String finished;
}
