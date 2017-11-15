package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Data;

@Data
public class ContactDetails {

    private final String phone;
    private final String email;
    private final String dxAddress;
}
