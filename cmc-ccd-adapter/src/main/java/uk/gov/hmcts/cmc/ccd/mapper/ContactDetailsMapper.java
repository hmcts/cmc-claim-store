package uk.gov.hmcts.cmc.ccd.mapper;

import uk.gov.hmcts.cmc.ccd.domain.ContactDetails;

public class ContactDetailsMapper {

    ContactDetails toCCD(uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails contactDetails) {
        return ContactDetails.builder()
            .email(contactDetails.getEmail().orElse(null))
            .phone(contactDetails.getPhone().orElse(null))
            .dxAddress(contactDetails.getDxAddress().orElse(null))
            .build();

    }

    uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails toCMC(ContactDetails contactDetails) {

        return new uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails(
            contactDetails.getPhone(), contactDetails.getEmail(), contactDetails.getDxAddress()
        );
    }
}
