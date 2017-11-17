package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

import java.util.Objects;

public class ContactDetailsAssert extends AbstractAssert<ContactDetailsAssert, ContactDetails> {

    public ContactDetailsAssert(ContactDetails contactDetails) {
        super(contactDetails, ContactDetailsAssert.class);
    }

    public ContactDetailsAssert isEqualTo(CCDContactDetails ccdContactDetails) {
        isNotNull();

        if (!Objects.equals(actual.getPhone().orElse(null), ccdContactDetails.getPhone())) {
            failWithMessage("Expected ContactDetails.phone to be <%s> but was <%s>",
                ccdContactDetails.getPhone(), actual.getPhone().orElse(null));
        }

        if (!Objects.equals(actual.getEmail().orElse(null), ccdContactDetails.getEmail())) {
            failWithMessage("Expected ContactDetails.email to be <%s> but was <%s>",
                ccdContactDetails.getEmail(), actual.getEmail().orElse(null));
        }

        if (!Objects.equals(actual.getDxAddress().orElse(null), ccdContactDetails.getDxAddress())) {
            failWithMessage("Expected ContactDetails.dxAddress to be <%s> but was <%s>",
                ccdContactDetails.getDxAddress(), actual.getDxAddress().orElse(null));
        }

        return this;
    }

}
