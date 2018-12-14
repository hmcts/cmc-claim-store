package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.mapper.AddressMapper;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.SOLE_TRADER;

@Component
public class DefendantPartyMapper {

    private final AddressMapper addressMapper;

    @Autowired
    public DefendantPartyMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public void to(CCDDefendant.CCDDefendantBuilder builder, Party party) {

        builder.partyName(party.getName());
        builder.partyAddress(addressMapper.to(party.getAddress()));
        party.getCorrespondenceAddress().ifPresent(
            address -> builder.partyCorrespondenceAddress(addressMapper.to(address))
        );
        party.getMobilePhone().ifPresent(builder::partyPhone);
        party.getRepresentative().ifPresent(representative -> toRepresentative(builder, representative));

        if (party instanceof Individual) {
            toIndividual(builder, (Individual) party);
        } else if (party instanceof Company) {
            toCompany(builder, (Company) party);
        } else if (party instanceof Organisation) {
            toOrganisation(builder, (Organisation) party);
        } else if (party instanceof SoleTrader) {
            toSoleTrader(builder, (SoleTrader) party);
        }
    }

    private void toIndividual(CCDDefendant.CCDDefendantBuilder builder, Individual individual) {
        builder.partyType(INDIVIDUAL);
        builder.partyDateOfBirth(individual.getDateOfBirth());
    }

    private void toRepresentative(CCDDefendant.CCDDefendantBuilder builder, Representative representative) {
        builder.representativeOrganisationName(representative.getOrganisationName());
        builder.representativeOrganisationAddress(addressMapper.to(representative.getOrganisationAddress()));
        representative.getOrganisationContactDetails().ifPresent(
            contactDetails -> {
                builder.representativeOrganisationPhone(contactDetails.getPhone().orElse(EMPTY));
                builder.representativeOrganisationEmail(contactDetails.getEmail().orElse(EMPTY));
                builder.representativeOrganisationDxAddress(contactDetails.getDxAddress().orElse(EMPTY));
            });
    }

    private void toSoleTrader(CCDDefendant.CCDDefendantBuilder builder, SoleTrader soleTrader) {
        builder.partyType(SOLE_TRADER);
        soleTrader.getTitle().ifPresent(builder::partyTitle);
        soleTrader.getBusinessName().ifPresent(builder::partyBusinessName);

    }

    private void toOrganisation(CCDDefendant.CCDDefendantBuilder builder, Organisation organisation) {
        builder.partyType(ORGANISATION);
        organisation.getContactPerson().ifPresent(builder::partyContactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(builder::partyCompaniesHouseNumber);
    }

    private void toCompany(CCDDefendant.CCDDefendantBuilder builder, Company company) {
        builder.partyType(COMPANY);
        company.getContactPerson().ifPresent(builder::partyContactPerson);
    }

}
