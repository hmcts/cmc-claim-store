package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;

@Component
public class OrganisationMapper {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;
    private final TelephoneMapper telephoneMapper;

    @Autowired
    public OrganisationMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper,
                              TelephoneMapper telephoneMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
        this.telephoneMapper = telephoneMapper;
    }

    public void to(Organisation organisation, CCDApplicant.CCDApplicantBuilder builder) {
        CCDParty.CCDPartyBuilder partyDetail = CCDParty.builder().type(CCDPartyType.ORGANISATION);
        organisation.getCorrespondenceAddress()
            .ifPresent(address -> partyDetail.correspondenceAddress(addressMapper.to(address)));
        organisation.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        organisation.getMobilePhone().ifPresent(telephoneNo -> partyDetail.telephoneNumber(
            telephoneMapper.to(telephoneNo)));
        organisation.getContactPerson().ifPresent(partyDetail::contactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(partyDetail::companiesHouseNumber);
        partyDetail.primaryAddress(addressMapper.to(organisation.getAddress()));
        builder
            .partyName(organisation.getName())
            .partyDetail(partyDetail.build());

    }

    public Organisation from(CCDCollectionElement<CCDApplicant> organisation) {
        CCDApplicant applicant = organisation.getValue();
        CCDParty partyDetail = applicant.getPartyDetail();
        return Organisation.builder()
            .id(organisation.getId())
            .name(applicant.getPartyName())
            .address(addressMapper.from(partyDetail.getPrimaryAddress()))
            .correspondenceAddress(addressMapper.from(partyDetail.getCorrespondenceAddress()))
            .mobilePhone(telephoneMapper.from(partyDetail.getTelephoneNumber()))
            .representative(representativeMapper.from(applicant))
            .contactPerson(partyDetail.getContactPerson())
            .companiesHouseNumber(partyDetail.getCompaniesHouseNumber())
            .build();
    }
}
