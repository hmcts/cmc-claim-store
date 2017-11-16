package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContactDetails {

    private final String phone;
    private final String email;
    private final String dxAddress;
}
