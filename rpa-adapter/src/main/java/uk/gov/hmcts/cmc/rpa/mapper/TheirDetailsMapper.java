package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.rpa.domain.Party;

import static java.time.format.DateTimeFormatter.ISO_DATE;

@Component("rpaTheirDetailMapper")
public class TheirDetailsMapper {
    private final AddressMapper rpaAddressMapper;

    public TheirDetailsMapper(AddressMapper rpaAddressMapper) {
        this.rpaAddressMapper = rpaAddressMapper;
    }

    public Party to(TheirDetails party) {
        Party.PartyBuilder builder = Party.builder();
        if (party instanceof IndividualDetails) {
            builder.type("individual");
            IndividualDetails individual = (IndividualDetails) party;
            updateIndividualDetails(builder, individual);
        } else if (party instanceof CompanyDetails) {
            builder.type("company");
            CompanyDetails company = (CompanyDetails) party;
            updateCompanyDetails(builder, company);
        } else if (party instanceof OrganisationDetails) {
            builder.type("organisation");
            OrganisationDetails organisation = (OrganisationDetails) party;
            updateOrganisationDetails(builder, organisation);
        } else if (party instanceof SoleTraderDetails) {
            builder.type("soleTrader");
            SoleTraderDetails soleTrader = (SoleTraderDetails) party;
            updateSoleTraderDetails(builder, soleTrader);
        }

        party.getServiceAddress().ifPresent(address -> builder.correspondenceAddress(rpaAddressMapper.to(address)));
        builder.fullAddress(rpaAddressMapper.to(party.getAddress()));
        builder.fullName(party.getName());
        return builder.build();
    }

    private void updateSoleTraderDetails(Party.PartyBuilder builder, SoleTraderDetails soleTrader) {
        soleTrader.getTitle().ifPresent(builder::title);
        soleTrader.getBusinessName().ifPresent(builder::businessName);
        soleTrader.getEmail().ifPresent(builder::emailAddress);
    }

    private void updateOrganisationDetails(Party.PartyBuilder builder, OrganisationDetails organisation) {
        organisation.getContactPerson().ifPresent(builder::contactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(builder::companiesHouseNumber);
        organisation.getEmail().ifPresent(builder::emailAddress);
    }

    private void updateCompanyDetails(Party.PartyBuilder builder, CompanyDetails company) {
        company.getContactPerson().ifPresent(builder::contactPerson);
        company.getEmail().ifPresent(builder::emailAddress);
    }

    private void updateIndividualDetails(Party.PartyBuilder builder, IndividualDetails individual) {
        individual.getDateOfBirth().ifPresent(dob -> builder.dateOfBirth(dob.format(ISO_DATE)));
    }
}
