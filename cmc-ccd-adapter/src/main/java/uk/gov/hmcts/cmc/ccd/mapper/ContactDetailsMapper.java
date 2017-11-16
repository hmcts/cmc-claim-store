package uk.gov.hmcts.cmc.ccd.mapper;

import uk.gov.hmcts.cmc.ccd.domain.ContactDetails;

public class ContactDetailsMapper
    implements Mapper<ContactDetails, uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails> {

    @Override
    public ContactDetails to(uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails contactDetails) {
        if (contactDetails == null) {
            return null;
        }
        return ContactDetails.builder()
            .email(contactDetails.getEmail().orElse(null))
            .phone(contactDetails.getPhone().orElse(null))
            .dxAddress(contactDetails.getDxAddress().orElse(null))
            .build();
    }

    @Override
    public uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails from(ContactDetails contactDetails) {
        if (contactDetails == null) {
            return null;
        }
        return new uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails(
            contactDetails.getPhone(), contactDetails.getEmail(), contactDetails.getDxAddress()
        );
    }
}
