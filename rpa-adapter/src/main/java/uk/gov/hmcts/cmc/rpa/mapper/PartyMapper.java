package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.rpa.domain.Party;

import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.MEDIUM_DATE_FORMAT;

@Component("rpaPartyMapper")
public class PartyMapper {

    private final AddressMapper rpaAddressMapper;

    public PartyMapper(AddressMapper rpaAddressMapper) {
        this.rpaAddressMapper = rpaAddressMapper;
    }

    public Party to(uk.gov.hmcts.cmc.domain.models.party.Party party) {
        Party.PartyBuilder builder = Party.builder();
        if (party instanceof Individual) {
            builder.type("individual");
            Individual individual = (Individual) party;
            updateIndividualDetails(builder, individual);
        } else if (party instanceof Company) {
            builder.type("company");
            Company company = (Company) party;
            updateCompanyDetails(builder, company);
        } else if (party instanceof Organisation) {
            builder.type("organisation");
            Organisation organisation = (Organisation) party;
            updateOrganisationDetails(builder, organisation);
        } else if (party instanceof SoleTrader) {
            builder.type("soleTrader");
            SoleTrader soleTrader = (SoleTrader) party;
            updateSoleTraderDetails(builder, soleTrader);
        }

        party.getMobilePhone().ifPresent(builder::phoneNumber);

        party.getCorrespondenceAddress()
            .ifPresent(address -> builder.correspondenceAddress(rpaAddressMapper.to(address)));

        builder.fullAddress(rpaAddressMapper.to(party.getAddress()));
        return builder.build();
    }

    private void updateSoleTraderDetails(Party.PartyBuilder builder, SoleTrader soleTrader) {
        soleTrader.getTitle().ifPresent(builder::title);
        soleTrader.getBusinessName().ifPresent(builder::businessName);
    }

    private void updateOrganisationDetails(Party.PartyBuilder builder, Organisation organisation) {
        organisation.getContactPerson().ifPresent(builder::contactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(builder::companiesHouseNumber);
    }

    private void updateCompanyDetails(Party.PartyBuilder builder, Company company) {
        company.getContactPerson().ifPresent(builder::contactPerson);
    }

    private void updateIndividualDetails(Party.PartyBuilder builder, Individual individual) {
        if (individual.getDateOfBirth() != null) {
            builder.dateOfBirth(individual.getDateOfBirth().format(MEDIUM_DATE_FORMAT));
        }
    }
}
