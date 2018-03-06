package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.SOLE_TRADER;

public class TheirDetailsAssert extends AbstractAssert<TheirDetailsAssert, TheirDetails> {

    public TheirDetailsAssert(TheirDetails party) {
        super(party, TheirDetailsAssert.class);
    }

    public TheirDetailsAssert isEqualTo(CCDParty ccdParty) {
        isNotNull();

        if (actual instanceof IndividualDetails) {
            if (!Objects.equals(INDIVIDUAL, ccdParty.getType())) {
                failWithMessage("Expected CCDParty.type to be <%s> but was <%s>",
                    ccdParty.getType(), INDIVIDUAL);
            }
            assertThat((IndividualDetails) actual).isEqualTo(ccdParty.getIndividual());
        }

        if (actual instanceof OrganisationDetails) {
            if (!Objects.equals(ORGANISATION, ccdParty.getType())) {
                failWithMessage("Expected CCDParty.type to be <%s> but was <%s>",
                    ccdParty.getType(), ORGANISATION);
            }
            assertThat((OrganisationDetails) actual).isEqualTo(ccdParty.getOrganisation());
        }

        if (actual instanceof CompanyDetails) {
            if (!Objects.equals(COMPANY, ccdParty.getType())) {
                failWithMessage("Expected CCDParty.type to be <%s> but was <%s>",
                    ccdParty.getType(), COMPANY);
            }
            assertThat((CompanyDetails) actual).isEqualTo(ccdParty.getCompany());
        }

        if (actual instanceof SoleTraderDetails) {
            if (!Objects.equals(SOLE_TRADER, ccdParty.getType())) {
                failWithMessage("Expected CCDParty.type to be <%s> but was <%s>",
                    ccdParty.getType(), SOLE_TRADER);
            }
            assertThat((SoleTraderDetails) actual).isEqualTo(ccdParty.getSoleTrader());
        }


        return this;
    }
}
