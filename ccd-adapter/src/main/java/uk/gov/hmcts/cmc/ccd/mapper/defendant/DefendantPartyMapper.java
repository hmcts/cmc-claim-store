package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.ccd.mapper.AddressMapper;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
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

    public void to(CCDRespondent.CCDRespondentBuilder builder, Party party) {
        requireNonNull(builder, "builder must not be null");
        requireNonNull(party, "party must not be null");

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

    private void toIndividual(CCDRespondent.CCDRespondentBuilder builder, Individual individual) {
        builder.partyType(INDIVIDUAL);
        builder.partyDateOfBirth(individual.getDateOfBirth());
    }

    private void toRepresentative(CCDRespondent.CCDRespondentBuilder builder, Representative representative) {
        builder.representativeOrganisationName(representative.getOrganisationName());
        builder.representativeOrganisationAddress(addressMapper.to(representative.getOrganisationAddress()));
        representative.getOrganisationContactDetails().ifPresent(
            contactDetails -> {
                contactDetails.getEmail().ifPresent(builder::representativeOrganisationEmail);
                contactDetails.getPhone().ifPresent(builder::representativeOrganisationPhone);
                contactDetails.getDxAddress().ifPresent(builder::representativeOrganisationDxAddress);
            });
    }

    private void toSoleTrader(CCDRespondent.CCDRespondentBuilder builder, SoleTrader soleTrader) {
        builder.partyType(SOLE_TRADER);
        soleTrader.getTitle().ifPresent(builder::partyTitle);
        soleTrader.getBusinessName().ifPresent(builder::partyBusinessName);
    }

    private void toOrganisation(CCDRespondent.CCDRespondentBuilder builder, Organisation organisation) {
        builder.partyType(ORGANISATION);
        organisation.getContactPerson().ifPresent(builder::partyContactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(builder::partyCompaniesHouseNumber);
    }

    private void toCompany(CCDRespondent.CCDRespondentBuilder builder, Company company) {
        builder.partyType(COMPANY);
        company.getContactPerson().ifPresent(builder::partyContactPerson);
    }

    public Party from(CCDRespondent respondent) {
        requireNonNull(respondent, "respondent must not be null");
        requireNonNull(respondent.getPartyType(), "respondent.getPartyType() must not be null");

        switch (respondent.getPartyType()) {
            case INDIVIDUAL:
                return extractIndividual(respondent);
            case COMPANY:
                return extractCompany(respondent);
            case SOLE_TRADER:
                return extractSoleTrader(respondent);
            case ORGANISATION:
                return extractOrganisation(respondent);
            default:
                throw new MappingException("Invalid partyType " + respondent.getPartyType());
        }
    }

    private Organisation extractOrganisation(CCDRespondent respondent) {
        return Organisation.builder()
            .name(respondent.getPartyName())
            .address(addressMapper.from(respondent.getPartyAddress()))
            .correspondenceAddress(addressMapper.from(respondent.getPartyCorrespondenceAddress()))
            .mobilePhone(respondent.getPartyPhone())
            .representative(extractRepresentative(respondent))
            .contactPerson(respondent.getPartyContactPerson())
            .companiesHouseNumber(respondent.getPartyCompaniesHouseNumber())
            .build();
    }

    private SoleTrader extractSoleTrader(CCDRespondent respondent) {
        return SoleTrader.builder()
            .name(respondent.getPartyName())
            .address(addressMapper.from(respondent.getPartyAddress()))
            .correspondenceAddress(addressMapper.from(respondent.getPartyCorrespondenceAddress()))
            .mobilePhone(respondent.getPartyPhone())
            .representative(extractRepresentative(respondent))
            .title(respondent.getPartyTitle())
            .businessName(respondent.getPartyBusinessName())
            .build();
    }

    private Company extractCompany(CCDRespondent respondent) {
        return Company.builder()
            .name(respondent.getPartyName())
            .address(addressMapper.from(respondent.getPartyAddress()))
            .correspondenceAddress(addressMapper.from(respondent.getPartyCorrespondenceAddress()))
            .mobilePhone(respondent.getPartyPhone())
            .representative(extractRepresentative(respondent))
            .contactPerson(respondent.getPartyContactPerson())
            .build();
    }

    private Individual extractIndividual(CCDRespondent respondent) {
        return Individual.builder()
            .name(respondent.getPartyName())
            .address(addressMapper.from(respondent.getPartyAddress()))
            .correspondenceAddress(addressMapper.from(respondent.getPartyCorrespondenceAddress()))
            .mobilePhone(respondent.getPartyPhone())
            .dateOfBirth(respondent.getPartyDateOfBirth())
            .representative(extractRepresentative(respondent))
            .build();
    }

    private Representative extractRepresentative(CCDRespondent respondent) {
        String organisationName = respondent.getRepresentativeOrganisationName();
        CCDAddress organisationAddress = respondent.getRepresentativeOrganisationAddress();

        if (isBlank(organisationName) && organisationAddress == null) {
            return null;
        }

        return Representative.builder()
            .organisationName(organisationName)
            .organisationAddress(addressMapper.from(organisationAddress))
            .organisationContactDetails(extractContactDetails(respondent))
            .build();
    }

    private ContactDetails extractContactDetails(CCDRespondent respondent) {
        return ContactDetails.builder()
            .phone(respondent.getRepresentativeOrganisationPhone())
            .email(respondent.getRepresentativeOrganisationEmail())
            .dxAddress(respondent.getRepresentativeOrganisationDxAddress())
            .build();
    }
}
