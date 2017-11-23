package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Objects;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class ClaimAssert extends AbstractAssert<ClaimAssert, Claim> {

    public ClaimAssert(Claim actual) {
        super(actual, ClaimAssert.class);
    }

    public ClaimAssert isEqualTo(CCDCase ccdCase) {
        isNotNull();

        if (!Objects.equals(actual.getReferenceNumber(), ccdCase.getReferenceNumber())) {
            failWithMessage("Expected CCDCase.referenceNumber to be <%s> but was <%s>",
                ccdCase.getReferenceNumber(), actual.getReferenceNumber());
        }

        if (!Objects.equals(actual.getSubmitterId(), ccdCase.getSubmitterId())) {
            failWithMessage("Expected CCDCase.submitterId to be <%s> but was <%s>",
                ccdCase.getSubmitterId(), actual.getSubmitterId());
        }

        if (!Objects.equals(actual.getCreatedAt().format(ISO_DATE_TIME), ccdCase.getSubmittedOn())) {
            failWithMessage("Expected CCDCase.submittedOn to be <%s> but was <%s>",
                ccdCase.getSubmittedOn(), actual.getCreatedAt().format(ISO_DATE_TIME));
        }

        if (!Objects.equals(actual.getExternalId(), ccdCase.getExternalId())) {
            failWithMessage("Expected CCDCase.externalId to be <%s> but was <%s>",
                ccdCase.getExternalId(), actual.getExternalId());
        }

        if (!Objects.equals(actual.getIssuedOn().format(ISO_DATE), ccdCase.getIssuedOn())) {
            failWithMessage("Expected CCDCase.issuedOn to be <%s> but was <%s>",
                ccdCase.getIssuedOn(), actual.getIssuedOn().format(ISO_DATE));
        }

        if (!Objects.equals(actual.getSubmitterEmail(), ccdCase.getSubmitterEmail())) {
            failWithMessage("Expected CCDCase.submitterEmail to be <%s> but was <%s>",
                ccdCase.getSubmitterEmail(), actual.getSubmitterEmail());
        }

        assertThat(actual.getClaimData()).isEqualTo(ccdCase.getClaim());

        return this;
    }

}
