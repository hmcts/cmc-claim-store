package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;

import java.util.Optional;
import java.util.function.Function;

@Component
public class SoleTraderDetailsMapper {

    private final AddressMapper addressMapper;
    private final DefendantRepresentativeMapper representativeMapper;
    private final TelephoneMapper telephoneMapper;

    @Autowired
    public SoleTraderDetailsMapper(AddressMapper addressMapper,
                                   DefendantRepresentativeMapper representativeMapper,
                                   TelephoneMapper telephoneMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
        this.telephoneMapper = telephoneMapper;
    }

    public void to(SoleTraderDetails soleTrader,
                   CCDRespondent.CCDRespondentBuilder builder) {

        CCDParty.CCDPartyBuilder claimantProvidedPartyDetail = CCDParty.builder().type(CCDPartyType.SOLE_TRADER);
        soleTrader.getTitle().ifPresent(claimantProvidedPartyDetail::title);
        soleTrader.getBusinessName().ifPresent(claimantProvidedPartyDetail::businessName);
        soleTrader.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        soleTrader.getEmail().ifPresent(claimantProvidedPartyDetail::emailAddress);
        soleTrader.getPhone()
            .ifPresent(phoneNo -> claimantProvidedPartyDetail.telephoneNumber(telephoneMapper.to(phoneNo)));

        claimantProvidedPartyDetail.primaryAddress(addressMapper.to(soleTrader.getAddress()));

        builder
            .claimantProvidedPartyName(soleTrader.getName())
            .claimantProvidedDetail(claimantProvidedPartyDetail.build());
    }

    public SoleTraderDetails from(CCDCollectionElement<CCDRespondent> ccdSoleTrader) {
        CCDRespondent respondent = ccdSoleTrader.getValue();
        CCDParty partyDetail = respondent.getPartyDetail();
        CCDParty detailFromClaimant = respondent.getClaimantProvidedDetail();

        return SoleTraderDetails.builder()
            .id(ccdSoleTrader.getId())
            .name(respondent.getClaimantProvidedPartyName())
            .email(getDetail(partyDetail, detailFromClaimant, x -> x.getEmailAddress()))
            .phoneNumber(telephoneMapper.from(getDetail(partyDetail, detailFromClaimant, CCDParty::getTelephoneNumber)))
            .address(getAddress(partyDetail, detailFromClaimant, CCDParty::getPrimaryAddress))
            .representative(representativeMapper.from(respondent))
            .title(detailFromClaimant.getTitle())
            .businessName(detailFromClaimant.getBusinessName())
            .serviceAddress(getAddress(partyDetail, detailFromClaimant, CCDParty::getCorrespondenceAddress))
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
