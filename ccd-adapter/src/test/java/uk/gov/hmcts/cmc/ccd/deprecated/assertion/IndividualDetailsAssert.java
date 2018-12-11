package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDIndividual;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;

import java.util.Objects;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class IndividualDetailsAssert extends AbstractAssert<IndividualDetailsAssert, IndividualDetails> {

    public IndividualDetailsAssert(IndividualDetails individual) {
        super(individual, IndividualDetailsAssert.class);
    }

    public IndividualDetailsAssert isEqualTo(CCDIndividual ccdIndividual) {
        isNotNull();

        if (!Objects.equals(actual.getName(), ccdIndividual.getName())) {
            failWithMessage("Expected CCDIndividual.name to be <%s> but was <%s>",
                ccdIndividual.getName(), actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), ccdIndividual.getEmail())) {
            failWithMessage("Expected CCDIndividual.email to be <%s> but was <%s>",
                ccdIndividual.getEmail(), actual.getEmail().orElse(null));
        }

        actual.getDateOfBirth()
            .ifPresent(dob -> assertThat(dob.format(ISO_DATE)).isEqualTo(ccdIndividual.getDateOfBirth()));


        assertThat(ccdIndividual.getAddress()).isEqualTo(actual.getAddress());
        actual.getServiceAddress()
            .ifPresent(address -> assertThat(ccdIndividual.getCorrespondenceAddress())
                .isEqualTo(address));
        actual.getRepresentative()
            .ifPresent(representative -> assertThat(ccdIndividual.getRepresentative())
                .isEqualTo(representative));

        return this;
    }
}
