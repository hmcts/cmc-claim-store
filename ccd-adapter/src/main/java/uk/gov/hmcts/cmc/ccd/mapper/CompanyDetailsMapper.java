package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;

@Component
public class CompanyDetailsMapper {

    private final AddressMapper addressMapper;
    private final DefendantRepresentativeMapper representativeMapper;

    @Autowired
    public CompanyDetailsMapper(AddressMapper addressMapper, DefendantRepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    public void to(CompanyDetails company, CCDRespondent.CCDRespondentBuilder builder) {
        CCDParty.CCDPartyBuilder applicantProvidedPartyDetail = CCDParty.builder().type(CCDPartyType.COMPANY);
        company.getEmail().ifPresent(applicantProvidedPartyDetail::emailAddress);
        company.getContactPerson().ifPresent(applicantProvidedPartyDetail::contactPerson);

        company.getServiceAddress()
            .ifPresent(address -> applicantProvidedPartyDetail.correspondenceAddress(addressMapper.to(address)));

        company.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        applicantProvidedPartyDetail.primaryAddress(addressMapper.to(company.getAddress()));

        builder
            .applicantProvidedPartyName(company.getName())
            .applicantProvidedDetail(applicantProvidedPartyDetail.build());

    }

    public CompanyDetails from(CCDCollectionElement<CCDRespondent> collectionElement) {
        CCDRespondent ccdRespondent = collectionElement.getValue();
        CCDParty applicantProvidedPartyDetail = ccdRespondent.getApplicantProvidedDetail();
        return CompanyDetails.builder()
            .id(collectionElement.getId())
            .name(ccdRespondent.getApplicantProvidedPartyName())
            .address(addressMapper.from(applicantProvidedPartyDetail.getPrimaryAddress()))
            .email(applicantProvidedPartyDetail.getEmailAddress())
            .representative(representativeMapper.from(ccdRespondent))
            .serviceAddress(addressMapper.from(applicantProvidedPartyDetail.getCorrespondenceAddress()))
            .contactPerson(applicantProvidedPartyDetail.getContactPerson())
            .build();
    }
}
