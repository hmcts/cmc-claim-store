package uk.gov.hmcts.cmc.domain.models.party;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class Organisation extends Party implements HasContactPerson {

    private final String contactPerson;
    private final String companiesHouseNumber;

    @Builder
    public Organisation(
        String id,
        String name,
        Address address,
        Address correspondenceAddress,
        String phone,
        String mobilePhone,
        Representative representative,
        String contactPerson,
        String companiesHouseNumber,
        String pcqId
    ) {
        super(id, name, address, correspondenceAddress, phone, mobilePhone, representative, pcqId);
        this.contactPerson = contactPerson;
        this.companiesHouseNumber = companiesHouseNumber;
    }

    public Optional<String> getContactPerson() {
        return Optional.ofNullable(contactPerson);
    }

    public Optional<String> getCompaniesHouseNumber() {
        return Optional.ofNullable(companiesHouseNumber);
    }

}
