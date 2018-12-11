package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

import java.util.Objects;

public class CCDContactDetailsAssert extends AbstractAssert<CCDContactDetailsAssert, CCDContactDetails> {

    public CCDContactDetailsAssert(CCDContactDetails ccdContactDetails) {
        super(ccdContactDetails, CCDContactDetailsAssert.class);
    }

    public CCDContactDetailsAssert isEqualTo(ContactDetails contactDetails) {
        isNotNull();

        if (!Objects.equals(actual.getPhone(), contactDetails.getPhone().orElse(null))) {
            failWithMessage("Expected CCDContactDetails.phone to be <%s> but was <%s>",
                contactDetails.getPhone().orElse(null), actual.getPhone());
        }

        if (!Objects.equals(actual.getEmail(), contactDetails.getEmail().orElse(null))) {
            failWithMessage("Expected CCDContactDetails.email to be <%s> but was <%s>",
                contactDetails.getEmail().orElse(null), actual.getEmail());
        }

        if (!Objects.equals(actual.getDxAddress(), contactDetails.getDxAddress().orElse(null))) {
            failWithMessage("Expected CCDContactDetails.dxAddress to be <%s> but was <%s>",
                contactDetails.getDxAddress().orElse(null), actual.getDxAddress());
        }

        return this;
    }

}
