package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.PersonContent;
import uk.gov.hmcts.cmc.domain.models.Address;

import static java.util.Objects.requireNonNull;

@Component
public class PersonContentProvider {

    public PersonContent createContent(
        String partyType,
        String name,
        Address address,
        Address correspondenceAddress,
        String email,
        String contactPerson,
        String businessName,
        String phoneNumber,
        String dateOfBirth
    ) {
        requireNonNull(name);
        requireNonNull(address);

        return PersonContent.builder()
            .partyType(partyType)
            .fullName(name)
            .address(address)
            .correspondenceAddress(correspondenceAddress)
            .email(email)
            .contactPerson(contactPerson)
            .businessName(businessName)
            .phoneNumber(phoneNumber)
            .dateOfBirth(dateOfBirth)
            .build();
    }

}
