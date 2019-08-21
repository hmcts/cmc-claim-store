package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.ioc.InitiatePaymentRequest;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertAmount;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertInterest;

public class InitiatePaymentRequestAssert extends AbstractAssert<InitiatePaymentRequestAssert, InitiatePaymentRequest> {

    public InitiatePaymentRequestAssert(InitiatePaymentRequest request) {
        super(request, InitiatePaymentRequestAssert.class);
    }

    public InitiatePaymentRequestAssert isEqualTo(CCDCase ccdCase) {
        isNotNull();

        if (!Objects.equals(actual.getIssuedOn(), ccdCase.getIssuedOn())) {
            failWithMessage("Expected CCDCase.issuedOn to be <%s> but was <%s>",
                ccdCase.getIssuedOn(), actual.getIssuedOn());
        }

        if (!Objects.equals(actual.getExternalId().toString(), ccdCase.getExternalId())) {
            failWithMessage("Expected CCDCase.externalId to be <%s> but was <%s>",
                ccdCase.getExternalId(), actual.getExternalId().toString());
        }

        assertAmount(actual.getAmount()).isMappedTo(ccdCase);
        assertInterest(actual.getInterest()).isMappedTo(ccdCase);

        return this;
    }
}
