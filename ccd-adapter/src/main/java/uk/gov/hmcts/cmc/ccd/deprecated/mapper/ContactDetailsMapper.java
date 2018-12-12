package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

@Component
public class ContactDetailsMapper implements Mapper<CCDContactDetails, ContactDetails> {

    @Override
    public CCDContactDetails to(ContactDetails contactDetails) {

        CCDContactDetails.CCDContactDetailsBuilder builder = CCDContactDetails.builder();
        contactDetails.getEmail().ifPresent(builder::email);
        contactDetails.getPhone().ifPresent(builder::phone);
        contactDetails.getDxAddress().ifPresent(builder::dxAddress);

        return builder.build();
    }

    @Override
    public ContactDetails from(CCDContactDetails contactDetails) {
        if (contactDetails == null) {
            return null;
        }

        return new ContactDetails(
            contactDetails.getPhone().orElse(null),
            contactDetails.getEmail().orElse(null),
            contactDetails.getDxAddress().orElse(null)
        );
    }
}
