package uk.gov.hmcts.cmc.claimstore.utils;

import uk.gov.hmcts.cmc.claimstore.exceptions.NotificationException;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.models.party.Company;
import uk.gov.hmcts.cmc.claimstore.models.party.Individual;
import uk.gov.hmcts.cmc.claimstore.models.party.Organisation;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;
import uk.gov.hmcts.cmc.claimstore.models.party.SoleTrader;

import java.util.Optional;

public class PartyUtils {

    private static final String INDIVIDUAL = "individual";
    private static final String SOLE_TRADER_OR_SELF_EMPLOYED_PERSON = "sole trader or self-employed person";
    private static final String ON_BEHALF_OF_A_COMPANY = "on behalf of a company";
    private static final String ON_BEHALF_OF_AN_ORGANISATION = "on behalf of an organisation";

    private PartyUtils() {
        // utility class, no instances
    }

    public static String getType(final Party claimant) {
        if (claimant instanceof Individual) {
            return INDIVIDUAL;
        } else if (claimant instanceof SoleTrader) {
            return SOLE_TRADER_OR_SELF_EMPLOYED_PERSON;
        } else if (claimant instanceof Company) {
            return ON_BEHALF_OF_A_COMPANY;
        } else if (claimant instanceof Organisation) {
            return ON_BEHALF_OF_AN_ORGANISATION;
        } else {
            throw new NotificationException("Claimant type is missing.");
        }
    }

    public static String getType(final TheirDetails defendant) {
        if (defendant instanceof IndividualDetails) {
            return INDIVIDUAL;
        } else if (defendant instanceof SoleTraderDetails) {
            return SOLE_TRADER_OR_SELF_EMPLOYED_PERSON;
        } else if (defendant instanceof CompanyDetails) {
            return ON_BEHALF_OF_A_COMPANY;
        } else if (defendant instanceof OrganisationDetails) {
            return ON_BEHALF_OF_AN_ORGANISATION;
        } else {
            throw new NotificationException("Defendant type is missing.");
        }
    }

    public static Optional<String> getDefendantContactPerson(final TheirDetails defendant) {
        if (defendant instanceof CompanyDetails)  {
            return ((CompanyDetails) defendant).getContactPerson();
        } else if (defendant instanceof OrganisationDetails) {
            return ((OrganisationDetails) defendant).getContactPerson();
        }
        return Optional.empty();
    }

    public static Optional<String> getDefendantBusinessName(final TheirDetails defendant) {
        if (defendant instanceof SoleTraderDetails) {
            return ((SoleTraderDetails) defendant).getBusinessName();
        }
        return Optional.empty();
    }

    public static Optional<String> getContactPerson(final Party claimant) {
        if (claimant instanceof Company) {
            return ((Company) claimant).getContactPerson();
        } else if (claimant instanceof Organisation) {
            return ((Organisation) claimant).getContactPerson();
        }
        return Optional.empty();
    }

    public static Optional<String> getBusinessName(final Party claimant) {
        if (claimant instanceof SoleTrader) {
            return ((SoleTrader) claimant).getBusinessName();
        }
        return Optional.empty();
    }

}
