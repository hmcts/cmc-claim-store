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

    @Autowired
    public SoleTraderDetailsMapper(AddressMapper addressMapper, DefendantRepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    public void to(SoleTraderDetails soleTrader,
                   CCDRespondent.CCDRespondentBuilder builder,
                   CCDParty.CCDPartyBuilder partyBuilder) {

        partyBuilder.type(CCDPartyType.SOLE_TRADER);
        soleTrader.getTitle().ifPresent(partyBuilder::title);
        soleTrader.getBusinessName().ifPresent(partyBuilder::businessName);
        soleTrader.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        soleTrader.getEmail().ifPresent(partyBuilder::emailAddress);
        soleTrader.getServiceAddress().ifPresent(address -> partyBuilder.primaryAddress(addressMapper.to(address)));

        CCDParty.CCDPartyBuilder claimantProvidedPartyDetail = CCDParty.builder();
        claimantProvidedPartyDetail.primaryAddress(addressMapper.to(soleTrader.getAddress()));

        builder
            .partyName(soleTrader.getName())
            .partyDetail(partyBuilder.build())
            .claimantProvidedDetail(claimantProvidedPartyDetail.build());
    }

    public SoleTraderDetails from(CCDCollectionElement<CCDRespondent> ccdSoleTrader) {
        CCDRespondent respondent = ccdSoleTrader.getValue();
        CCDParty partyDetails = respondent.getPartyDetail();
        CCDParty claimantProvidedPartyDetails = respondent.getClaimantProvidedDetail();

        return SoleTraderDetails.builder()
            .id(ccdSoleTrader.getId())
            .name(respondent.getPartyName())
            .address(addressMapper.from(partyDetails.getPrimaryAddress()))
            .email(partyDetails.getEmailAddress())
            .representative(representativeMapper.from(respondent))
            .serviceAddress(addressMapper.from(claimantProvidedPartyDetails.getPrimaryAddress()))
            .title(partyDetails.getTitle())
            .businessName(partyDetails.getBusinessName())
            .build();
    }
}
