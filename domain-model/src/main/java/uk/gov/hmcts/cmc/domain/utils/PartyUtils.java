package uk.gov.hmcts.cmc.domain.utils;

import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import java.time.LocalDate;
import java.util.Optional;

public class PartyUtils {

    static final String INDIVIDUAL = "as an individual";
    static final String SOLE_TRADER_OR_SELF_EMPLOYED_PERSON = "as a sole trader or self-employed person";
    static final String ON_BEHALF_OF_A_COMPANY = "on behalf of a company";
    static final String ON_BEHALF_OF_AN_ORGANISATION = "on behalf of an organisation";

    private PartyUtils() {
        // utility class, no instances
    }

    public static String getType(Party party) {
        if (party instanceof Individual) {
            return INDIVIDUAL;
        } else if (party instanceof SoleTrader) {
            return SOLE_TRADER_OR_SELF_EMPLOYED_PERSON;
        } else if (party instanceof Company) {
            return ON_BEHALF_OF_A_COMPANY;
        } else if (party instanceof Organisation) {
            return ON_BEHALF_OF_AN_ORGANISATION;
        } else {
            throw new NotificationException("Claimant type is missing.");
        }
    }

    public static String getType(TheirDetails party) {
        if (party instanceof IndividualDetails) {
            return INDIVIDUAL;
        } else if (party instanceof SoleTraderDetails) {
            return SOLE_TRADER_OR_SELF_EMPLOYED_PERSON;
        } else if (party instanceof CompanyDetails) {
            return ON_BEHALF_OF_A_COMPANY;
        } else if (party instanceof OrganisationDetails) {
            return ON_BEHALF_OF_AN_ORGANISATION;
        } else {
            throw new NotificationException("Defendant type is missing.");
        }
    }

    public static Optional<String> getContactPerson(Party party) {
        if (party instanceof Company) {
            return ((Company) party).getContactPerson();
        } else if (party instanceof Organisation) {
            return ((Organisation) party).getContactPerson();
        }
        return Optional.empty();
    }

    public static Optional<String> getContactPerson(TheirDetails party) {
        if (party instanceof CompanyDetails) {
            return ((CompanyDetails) party).getContactPerson();
        } else if (party instanceof OrganisationDetails) {
            return ((OrganisationDetails) party).getContactPerson();
        }
        return Optional.empty();
    }

    public static Optional<String> getBusinessName(Party party) {
        if (party instanceof SoleTrader) {
            return ((SoleTrader) party).getBusinessName();
        }
        return Optional.empty();
    }

    public static Optional<String> getBusinessName(TheirDetails party) {
        if (party instanceof SoleTraderDetails) {
            return ((SoleTraderDetails) party).getBusinessName();
        }
        return Optional.empty();
    }

    public static Optional<LocalDate> claimantDateOfBirth(Party party) {
        if (party instanceof Individual) {
            return Optional.of(((Individual) party).getDateOfBirth());
        }
        return Optional.empty();
    }

}
