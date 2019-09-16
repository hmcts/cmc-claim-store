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
    private final TelephoneMapper telephoneMapper;

    @Autowired
    public CompanyDetailsMapper(AddressMapper addressMapper,
                                DefendantRepresentativeMapper representativeMapper,
                                TelephoneMapper telephoneMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
        this.telephoneMapper = telephoneMapper;
    }

    public void to(CompanyDetails company,
                   CCDRespondent.CCDRespondentBuilder builder) {
        CCDParty.CCDPartyBuilder claimantProvidedPartyDetail = CCDParty.builder().type(CCDPartyType.COMPANY);
        company.getEmail().ifPresent(claimantProvidedPartyDetail::emailAddress);
        company.getContactPerson().ifPresent(claimantProvidedPartyDetail::contactPerson);
        company.getPhone()
            .ifPresent(phoneNo -> claimantProvidedPartyDetail.telephoneNumber(telephoneMapper.to(phoneNo)));
        company.getServiceAddress()
            .ifPresent(address -> claimantProvidedPartyDetail.correspondenceAddress(addressMapper.to(address)));

        company.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        claimantProvidedPartyDetail.primaryAddress(addressMapper.to(company.getAddress()));

        builder
            .claimantProvidedPartyName(company.getName())
            .claimantProvidedDetail(claimantProvidedPartyDetail.build());

    }

    public CompanyDetails from(CCDCollectionElement<CCDRespondent> collectionElement) {
        CCDRespondent ccdRespondent = collectionElement.getValue();
        CCDParty claimantProvidedPartyDetail = ccdRespondent.getClaimantProvidedDetail();
        return CompanyDetails.builder()
            .id(collectionElement.getId())
            .name(ccdRespondent.getClaimantProvidedPartyName())
            .address(addressMapper.from(claimantProvidedPartyDetail.getPrimaryAddress()))
            .email(claimantProvidedPartyDetail.getEmailAddress())
            .phoneNumber(telephoneMapper.from(claimantProvidedPartyDetail.getTelephoneNumber()))
            .representative(representativeMapper.from(ccdRespondent))
            .serviceAddress(addressMapper.from(claimantProvidedPartyDetail.getCorrespondenceAddress()))
            .contactPerson(claimantProvidedPartyDetail.getContactPerson())
            .build();
    }
}
