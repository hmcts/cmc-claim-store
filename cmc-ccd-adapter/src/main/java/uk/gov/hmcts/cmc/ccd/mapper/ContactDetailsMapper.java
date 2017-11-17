package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

@Component
public class ContactDetailsMapper implements Mapper<CCDContactDetails, ContactDetails> {

    @Override
    public CCDContactDetails to(ContactDetails contactDetails) {

        return CCDContactDetails.builder()
            .email(contactDetails.getEmail().orElse(null))
            .phone(contactDetails.getPhone().orElse(null))
            .dxAddress(contactDetails.getDxAddress().orElse(null))
            .build();
    }

    @Override
    public ContactDetails from(CCDContactDetails contactDetails) {

        return new ContactDetails(
            contactDetails.getPhone(), contactDetails.getEmail(), contactDetails.getDxAddress()
        );
    }
}
