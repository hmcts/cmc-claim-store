package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;

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
        CCDParty claimantProvidedPartyDetails = respondent.getClaimantProvidedDetail();

        return SoleTraderDetails.builder()
            .id(ccdSoleTrader.getId())
            .name(respondent.getClaimantProvidedPartyName())
            .email(claimantProvidedPartyDetails.getEmailAddress())
            .phoneNumber(telephoneMapper.from(claimantProvidedPartyDetails.getTelephoneNumber()))
            .address(addressMapper.from(claimantProvidedPartyDetails.getPrimaryAddress()))
            .representative(representativeMapper.from(respondent))
            .title(claimantProvidedPartyDetails.getTitle())
            .businessName(claimantProvidedPartyDetails.getBusinessName())
            .serviceAddress(addressMapper.from(claimantProvidedPartyDetails.getCorrespondenceAddress()))
            .build();
    }
}
