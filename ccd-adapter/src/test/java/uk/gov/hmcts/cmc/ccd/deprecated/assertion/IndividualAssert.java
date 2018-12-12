package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDIndividual;
import uk.gov.hmcts.cmc.domain.models.party.Individual;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class IndividualAssert extends AbstractAssert<IndividualAssert, Individual> {

    public IndividualAssert(Individual individual) {
        super(individual, IndividualAssert.class);
    }

    public IndividualAssert isEqualTo(CCDIndividual ccdIndividual) {
        isNotNull();

        if (!Objects.equals(actual.getName(), ccdIndividual.getName())) {
            failWithMessage("Expected CCDIndividual.name to be <%s> but was <%s>",
                ccdIndividual.getName(), actual.getName());
        }

        if (!Objects.equals(actual.getMobilePhone().orElse(null), ccdIndividual.getPhoneNumber())) {
            failWithMessage("Expected CCDIndividual.mobilePhone to be <%s> but was <%s>",
                ccdIndividual.getPhoneNumber(), actual.getMobilePhone().orElse(null));
        }

        if (actual.getDateOfBirth() != null) {
            String dateOfBirth = actual.getDateOfBirth().format(DateTimeFormatter.ISO_DATE);
            if (!Objects.equals(dateOfBirth, ccdIndividual.getDateOfBirth())) {
                failWithMessage("Expected CCDIndividual.dateOfBirth to be <%s> but was <%s>",
                    ccdIndividual.getDateOfBirth(), dateOfBirth);
            }
        }

        assertThat(ccdIndividual.getAddress()).isEqualTo(actual.getAddress());
        actual.getCorrespondenceAddress()
            .ifPresent(address -> assertThat(ccdIndividual.getCorrespondenceAddress())
                .isEqualTo(address));
        actual.getRepresentative()
            .ifPresent(representative -> assertThat(ccdIndividual.getRepresentative())
                .isEqualTo(representative));

        return this;
    }
}
