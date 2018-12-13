package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

import java.util.Objects;

public class ContactDetailsAssert extends AbstractAssert<ContactDetailsAssert, ContactDetails> {

    public ContactDetailsAssert(ContactDetails contactDetails) {
        super(contactDetails, ContactDetailsAssert.class);
    }

    public ContactDetailsAssert isEqualTo(CCDContactDetails ccdContactDetails) {
        isNotNull();

        String phoneActual = actual.getPhone().orElse(null);
        String phone = ccdContactDetails.getPhone().orElse(null);
        if (!Objects.equals(phoneActual, phone)) {
            failWithMessage("Expected ContactDetails.phone to be <%s> but was <%s>",
                phone, phoneActual);
        }

        String emailActual = actual.getEmail().orElse(null);
        String email = ccdContactDetails.getEmail().orElse(null);
        if (!Objects.equals(emailActual, email)) {
            failWithMessage("Expected ContactDetails.email to be <%s> but was <%s>",
                email, emailActual);
        }

        String dxAddressActual = actual.getDxAddress().orElse(null);
        String dxAddress = ccdContactDetails.getDxAddress().orElse(null);
        if (!Objects.equals(dxAddressActual, dxAddress)) {
            failWithMessage("Expected ContactDetails.dxAddress to be <%s> but was <%s>",
                dxAddress, dxAddressActual);
        }

        return this;
    }

}
