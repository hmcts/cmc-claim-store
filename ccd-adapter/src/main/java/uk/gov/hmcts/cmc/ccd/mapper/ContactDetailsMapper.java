package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

@Component
public class ContactDetailsMapper implements Mapper<CCDContactDetails, ContactDetails> {

    @Override
    public CCDContactDetails to(ContactDetails contactDetails) {

        final CCDContactDetails.CCDContactDetailsBuilder builder = CCDContactDetails.builder();
        contactDetails.getEmail().ifPresent(builder::email);
        contactDetails.getPhone().ifPresent(builder::phone);
        contactDetails.getDxAddress().ifPresent(builder::dxAddress);

        return builder.build();
    }

    @Override
    public ContactDetails from(CCDContactDetails contactDetails) {

        return new ContactDetails(
            contactDetails.getPhone().orElse(null),
            contactDetails.getEmail().orElse(null),
            contactDetails.getDxAddress().orElse(null)
        );
    }
}
