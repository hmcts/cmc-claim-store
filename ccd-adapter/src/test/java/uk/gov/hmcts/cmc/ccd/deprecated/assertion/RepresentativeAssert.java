package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class RepresentativeAssert extends AbstractAssert<RepresentativeAssert, CCDRepresentative> {

    public RepresentativeAssert(CCDRepresentative representative) {
        super(representative, RepresentativeAssert.class);
    }

    public RepresentativeAssert isEqualTo(Representative representative) {
        isNotNull();

        if (!Objects.equals(actual.getOrganisationName(), representative.getOrganisationName())) {
            failWithMessage("Expected CCDRepresentative.organisationName to be <%s> but was <%s>",
                representative.getOrganisationName(), actual.getOrganisationName());
        }

        assertThat(representative.getOrganisationAddress()).isEqualTo(actual.getOrganisationAddress());
        assertThat(representative.getOrganisationContactDetails().orElse(null))
            .isEqualTo(actual.getOrganisationContactDetails().orElse(null));

        return this;
    }
}
