package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;

import java.util.Optional;
import java.util.function.Function;

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
        CCDParty detailFromClaimant = ccdRespondent.getClaimantProvidedDetail();
        CCDParty partyDetail = ccdRespondent.getPartyDetail();

        return CompanyDetails.builder()
            .id(collectionElement.getId())
            .name(ccdRespondent.getClaimantProvidedPartyName())
            .address(getAddress(partyDetail, detailFromClaimant, CCDParty::getPrimaryAddress))
            .email(getDetail(partyDetail, detailFromClaimant, x -> x.getEmailAddress()))
            .phoneNumber(telephoneMapper.from(getDetail(partyDetail, detailFromClaimant, CCDParty::getTelephoneNumber)))
            .representative(representativeMapper.from(ccdRespondent))
            .serviceAddress(getAddress(partyDetail, detailFromClaimant, CCDParty::getCorrespondenceAddress))
            .contactPerson(detailFromClaimant.getContactPerson())
            .build();
    }

    private Address getAddress(CCDParty detail, CCDParty detailByClaimant, Function<CCDParty, CCDAddress> getAddress) {
        CCDAddress partyAddress = getAddress(detail, getAddress);
        return addressMapper.from(partyAddress != null ? partyAddress : getAddress(detailByClaimant, getAddress));
    }

    private CCDAddress getAddress(CCDParty partyDetail, Function<CCDParty, CCDAddress> extractAddress) {
        return Optional.ofNullable(partyDetail).map(extractAddress).orElse(null);
    }

    private <T> T getDetail(CCDParty detail, CCDParty detailByClaimant, Function<CCDParty, T> getDetail) {
        T partyDetail = detail != null ? getDetail.apply(detail) : null;
        return partyDetail != null ? partyDetail : getDetail.apply(detailByClaimant);
    }

}
