package uk.gov.hmcts.cmc.domain.models.legalrep;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.Email;
import uk.gov.hmcts.cmc.domain.constraints.PhoneNumber;

import java.util.Optional;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class ContactDetails {

    @PhoneNumber
    private final String phone;

    @Email
    private final String email;

    @Size(max = 255, message = "must be at most {max} characters")
    private final String dxAddress;

    public ContactDetails(String phone, String email, String dxAddress) {
        this.phone = phone;
        this.email = email;
        this.dxAddress = dxAddress;
    }

    public Optional<String> getPhone() {
        return Optional.ofNullable(phone);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<String> getDxAddress() {
        return Optional.ofNullable(dxAddress);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
