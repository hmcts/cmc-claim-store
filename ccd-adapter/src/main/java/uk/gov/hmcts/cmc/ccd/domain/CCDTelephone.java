package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CCDTelephone {
    private String telephoneNumber;
    private String telephoneUsageType;
    private String contactDirection;
}
