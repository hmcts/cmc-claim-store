package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

@Component
public class SoleTraderMapper {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public SoleTraderMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    public void to(SoleTrader soleTrader, CCDClaimant.CCDClaimantBuilder builder) {

        soleTrader.getTitle().ifPresent(builder::partyTitle);
        soleTrader.getMobilePhone().ifPresent(builder::partyPhone);
        soleTrader.getBusinessName().ifPresent(builder::partyBusinessName);
        soleTrader.getCorrespondenceAddress()
            .ifPresent(address -> builder.partyCorrespondenceAddress(addressMapper.to(address)));
        soleTrader.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        builder
            .partyName(soleTrader.getName())
            .partyAddress(addressMapper.to(soleTrader.getAddress()));

    }

    public SoleTrader from(CCDCollectionElement<CCDClaimant> soletrader) {
        CCDClaimant value = soletrader.getValue();
        return SoleTrader.builder()
            .id(soletrader.getId())
            .name(value.getPartyName())
            .address(addressMapper.from(value.getPartyAddress()))
            .correspondenceAddress(addressMapper.from(value.getPartyCorrespondenceAddress()))
            .mobilePhone(value.getPartyPhone())
            .representative(representativeMapper.from(value))
            .title(value.getPartyTitle())
            .businessName(value.getPartyBusinessName())
            .build();
    }
}
