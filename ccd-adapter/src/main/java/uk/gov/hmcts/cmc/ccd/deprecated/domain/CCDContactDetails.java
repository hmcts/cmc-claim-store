package uk.gov.hmcts.cmc.ccd.deprecated.domain;

import lombok.Builder;
import lombok.Value;

import java.util.Optional;

@Value
@Builder
public class CCDContactDetails {

    private String phone;
    private String email;
    private String dxAddress;

    public Optional<String> getPhone() {
        return Optional.ofNullable(phone);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<String> getDxAddress() {
        return Optional.ofNullable(dxAddress);
    }
}
