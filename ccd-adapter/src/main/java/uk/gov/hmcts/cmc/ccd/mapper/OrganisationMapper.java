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

    public void to(Organisation organisation, CCDApplicant.CCDApplicantBuilder builder,
                   CCDParty.CCDPartyBuilder applicantPartyDetail) {
        applicantPartyDetail.type(CCDPartyType.ORGANISATION);
        organisation.getCorrespondenceAddress()
            .ifPresent(address -> applicantPartyDetail.correspondenceAddress(addressMapper.to(address)));
        organisation.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        organisation.getPhone().ifPresent(telephoneNo -> applicantPartyDetail.telephoneNumber(
            telephoneMapper.to(telephoneNo)));
        organisation.getContactPerson().ifPresent(applicantPartyDetail::contactPerson);
        organisation.getCompaniesHouseNumber().ifPresent(applicantPartyDetail::companiesHouseNumber);
        applicantPartyDetail.primaryAddress(addressMapper.to(organisation.getAddress()));
        builder
            .partyName(organisation.getName())
            .partyDetail(applicantPartyDetail.build());

    }

    public Organisation from(CCDCollectionElement<CCDApplicant> organisation) {
        CCDApplicant applicant = organisation.getValue();
        CCDParty partyDetail = applicant.getPartyDetail();
        return Organisation.builder()
            .id(organisation.getId())
            .name(applicant.getPartyName())
            .address(addressMapper.from(partyDetail.getPrimaryAddress()))
            .correspondenceAddress(addressMapper.from(partyDetail.getCorrespondenceAddress()))
            .phone(telephoneMapper.from(partyDetail.getTelephoneNumber()))
            .representative(representativeMapper.from(applicant))
            .contactPerson(partyDetail.getContactPerson())
            .companiesHouseNumber(partyDetail.getCompaniesHouseNumber())
            .build();
    }
}
