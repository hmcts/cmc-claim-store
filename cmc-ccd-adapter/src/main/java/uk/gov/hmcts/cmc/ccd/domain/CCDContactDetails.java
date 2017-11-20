package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class CCDContactDetails {

    private final String phone;
    private final String email;
    private final String dxAddress;

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
