package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDParty;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType.SOLE_TRADER;

public class PartyAssert extends AbstractAssert<PartyAssert, Party> {

    public PartyAssert(Party party) {
        super(party, PartyAssert.class);
    }

    public PartyAssert isEqualTo(CCDParty ccdParty) {
        isNotNull();

        if (actual instanceof Individual) {
            if (!Objects.equals(INDIVIDUAL, ccdParty.getType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    ccdParty.getType(), INDIVIDUAL);
            }
            assertThat((Individual) actual).isEqualTo(ccdParty.getIndividual());
        }

        if (actual instanceof Organisation) {
            if (!Objects.equals(ORGANISATION, ccdParty.getType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    ccdParty.getType(), ORGANISATION);
            }
            assertThat((Organisation) actual).isEqualTo(ccdParty.getOrganisation());
        }

        if (actual instanceof Company) {
            if (!Objects.equals(COMPANY, ccdParty.getType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    ccdParty.getType(), COMPANY);
            }
            assertThat((Company) actual).isEqualTo(ccdParty.getCompany());
        }

        if (actual instanceof SoleTrader) {
            if (!Objects.equals(SOLE_TRADER, ccdParty.getType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    ccdParty.getType(), SOLE_TRADER);
            }
            assertThat((SoleTrader) actual).isEqualTo(ccdParty.getSoleTrader());
        }


        return this;
    }
}
