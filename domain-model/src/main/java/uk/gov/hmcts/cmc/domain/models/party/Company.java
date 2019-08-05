package uk.gov.hmcts.cmc.domain.models.party;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class Company extends Party implements HasContactPerson {

    private final String contactPerson;

    @Builder
    public Company(
        String id,
        String name,
        Address address,
        Address correspondenceAddress,
        String phone,
        String mobilePhone,
        Representative representative,
        String contactPerson
    ) {
        super(id, name, address, correspondenceAddress, phone, mobilePhone, representative);
        this.contactPerson = contactPerson;
    }

    public Optional<String> getContactPerson() {
        return Optional.ofNullable(contactPerson);
    }

}
