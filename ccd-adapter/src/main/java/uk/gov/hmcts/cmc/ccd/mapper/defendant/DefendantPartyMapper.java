package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.ccd.mapper.AddressMapper;
import uk.gov.hmcts.cmc.ccd.mapper.TelephoneMapper;
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
    private final TelephoneMapper telephoneMapper;

    @Autowired
    public DefendantPartyMapper(AddressMapper addressMapper, TelephoneMapper telephoneMapper) {
        this.addressMapper = addressMapper;
        this.telephoneMapper = telephoneMapper;
    }

    public void to(CCDRespondent.CCDRespondentBuilder builder, Party party) {
        requireNonNull(builder, "builder must not be null");
        requireNonNull(party, "party must not be null");

        builder.partyName(party.getName());
        CCDParty.CCDPartyBuilder respondentDetail = CCDParty.builder();
        respondentDetail.primaryAddress(addressMapper.to(party.getAddress()));
        party.getCorrespondenceAddress().ifPresent(
            address -> respondentDetail.correspondenceAddress(addressMapper.to(address))
        );
        party.getMobilePhone()
            .ifPresent(telephoneNo -> respondentDetail.telephoneNumber(telephoneMapper.to(telephoneNo)));
        party.getRepresentative().ifPresent(representative -> toRepresentative(builder, representative));

        if (party instanceof Individual) {
            toIndividual(respondentDetail, (Individual) party);
        } else if (party instanceof Company) {
            toCompany(respondentDetail, (Company) party);
        } else if (party instanceof Organisation) {
            toOrganisation(respondentDetail, (Organisation) party);
        } else if (party instanceof SoleTrader) {
            toSoleTrader(respondentDetail, (SoleTrader) party);
        }
        builder.partyDetail(respondentDetail.build());
    }

    private void toIndividual(CCDParty.CCDPartyBuilder partyBuilder, Individual individual) {
        partyBuilder.type(INDIVIDUAL);
        partyBuilder.dateOfBirth(individual.getDateOfBirth());
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

    private void toSoleTrader(CCDParty.CCDPartyBuilder partyBuilder, SoleTrader soleTrader) {
        partyBuilder.type(SOLE_TRADER);
        soleTrader.getTitle().ifPresent(partyBuilder::title);
        soleTrader.getBusinessName().ifPresent(partyBuilder::businessName);
    }

    private void toOrganisation(CCDParty.CCDPartyBuilder partyBuilder, Organisation organisation) {
        partyBuilder.type(ORGANISATION);
        organisation.getContactPerson().ifPresent(partyBuilder::contactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(partyBuilder::companiesHouseNumber);
    }

    private void toCompany(CCDParty.CCDPartyBuilder partyBuilder, Company company) {
        partyBuilder.type(COMPANY);
        company.getContactPerson().ifPresent(partyBuilder::contactPerson);
    }

    public Party from(CCDRespondent respondent) {
        requireNonNull(respondent, "respondent must not be null");
        requireNonNull(respondent.getPartyDetail(), "respondent.getPartyDetail() must not be null");

        switch (respondent.getPartyDetail().getType()) {
            case INDIVIDUAL:
                return extractIndividual(respondent);
            case COMPANY:
                return extractCompany(respondent);
            case SOLE_TRADER:
                return extractSoleTrader(respondent);
            case ORGANISATION:
                return extractOrganisation(respondent);
            default:
                throw new MappingException("Invalid partyType " + respondent.getPartyDetail().getType());
        }
    }

    private Organisation extractOrganisation(CCDRespondent respondent) {
        CCDParty partyDetail = respondent.getPartyDetail();
        return Organisation.builder()
            .name(respondent.getPartyName())
            .address(addressMapper.from(partyDetail.getPrimaryAddress()))
            .correspondenceAddress(addressMapper.from(partyDetail.getCorrespondenceAddress()))
            .mobilePhone(telephoneMapper.from(partyDetail.getTelephoneNumber()))
            .representative(extractRepresentative(respondent))
            .contactPerson(partyDetail.getContactPerson())
            .companiesHouseNumber(partyDetail.getCompaniesHouseNumber())
            .build();
    }

    private SoleTrader extractSoleTrader(CCDRespondent respondent) {
        CCDParty partyDetail = respondent.getPartyDetail();
        return SoleTrader.builder()
            .name(respondent.getPartyName())
            .address(addressMapper.from(partyDetail.getPrimaryAddress()))
            .correspondenceAddress(addressMapper.from(partyDetail.getCorrespondenceAddress()))
            .mobilePhone(telephoneMapper.from(partyDetail.getTelephoneNumber()))
            .representative(extractRepresentative(respondent))
            .title(partyDetail.getTitle())
            .businessName(partyDetail.getBusinessName())
            .build();
    }

    private Company extractCompany(CCDRespondent respondent) {
        CCDParty partyDetail = respondent.getPartyDetail();
        return Company.builder()
            .name(respondent.getPartyName())
            .address(addressMapper.from(partyDetail.getPrimaryAddress()))
            .correspondenceAddress(addressMapper.from(partyDetail.getCorrespondenceAddress()))
            .mobilePhone(telephoneMapper.from(partyDetail.getTelephoneNumber()))
            .representative(extractRepresentative(respondent))
            .contactPerson(partyDetail.getContactPerson())
            .build();
    }

    private Individual extractIndividual(CCDRespondent respondent) {
        CCDParty partyDetail = respondent.getPartyDetail();
        return Individual.builder()
            .name(respondent.getPartyName())
            .address(addressMapper.from(partyDetail.getPrimaryAddress()))
            .correspondenceAddress(addressMapper.from(partyDetail.getCorrespondenceAddress()))
            .mobilePhone(telephoneMapper.from(partyDetail.getTelephoneNumber()))
            .dateOfBirth(partyDetail.getDateOfBirth())
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
