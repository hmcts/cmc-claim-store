package uk.gov.hmcts.cmc.ccd.adapter.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.domain.models.party.Company;

@Component
public class CompanyMapper {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;
    private final TelephoneMapper telephoneMapper;

    @Autowired
    public CompanyMapper(AddressMapper addressMapper,
                         RepresentativeMapper representativeMapper,
                         TelephoneMapper telephoneMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
        this.telephoneMapper = telephoneMapper;
    }

    public void to(Company company,
                   CCDApplicant.CCDApplicantBuilder builder,
                   CCDParty.CCDPartyBuilder applicantPartyDetail) {

        applicantPartyDetail.type(CCDPartyType.COMPANY);
        company.getMobilePhone()
            .ifPresent(mobileNo -> applicantPartyDetail.telephoneNumber(telephoneMapper.to(mobileNo)));
        company.getContactPerson().ifPresent(applicantPartyDetail::contactPerson);

        company.getCorrespondenceAddress()
            .ifPresent(address -> applicantPartyDetail.correspondenceAddress(addressMapper.to(address)));

        company.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));

        applicantPartyDetail.primaryAddress(addressMapper.to(company.getAddress()));

        builder
            .partyName(company.getName())
            .partyDetail(applicantPartyDetail.build());

    }

    public Company from(CCDCollectionElement<CCDApplicant> company) {
        CCDApplicant applicant = company.getValue();
        CCDParty partyDetail = applicant.getPartyDetail();
        return Company.builder()
            .id(company.getId())
            .name(applicant.getPartyName())
            .address(addressMapper.from(partyDetail.getPrimaryAddress()))
            .correspondenceAddress(addressMapper.from(partyDetail.getCorrespondenceAddress()))
            .mobilePhone(telephoneMapper.from(partyDetail.getTelephoneNumber()))
            .representative(representativeMapper.from(applicant))
            .contactPerson(partyDetail.getContactPerson())
            .build();
    }
}
