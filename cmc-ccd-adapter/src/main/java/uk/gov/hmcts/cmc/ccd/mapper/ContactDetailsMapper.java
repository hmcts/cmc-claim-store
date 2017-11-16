package uk.gov.hmcts.cmc.ccd.mapper;

import uk.gov.hmcts.cmc.ccd.domain.ContactDetails;

public class ContactDetailsMapper {

    ContactDetails toCCD(uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails contactDetails) {
        ContactDetails ccdContactDetails = ContactDetails.builder()
            .email(contactDetails.getEmail().orElse(null))
            .phone(contactDetails.getPhone().orElse(null))
            .dxAddress(contactDetails.getDxAddress().orElse(null))
            .build();

        return ccdContactDetails;
    }

    uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails toCMC(ContactDetails contactDetails) {
        uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails cmcContactDetails =
            new uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails(
                contactDetails.getPhone(), contactDetails.getEmail(), contactDetails.getDxAddress()
            );
        return cmcContactDetails;
    }
}
