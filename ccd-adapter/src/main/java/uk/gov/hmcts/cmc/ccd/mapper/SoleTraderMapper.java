package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

@Component
public class SoleTraderMapper implements BuilderMapper<CCDClaimant, SoleTrader, CCDClaimant.CCDClaimantBuilder> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public SoleTraderMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
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

    @Override
    public SoleTrader from(CCDClaimant ccdSoleTrader) {
        return new SoleTrader(
            ccdSoleTrader.getPartyName(),
            addressMapper.from(ccdSoleTrader.getPartyAddress()),
            addressMapper.from(ccdSoleTrader.getPartyCorrespondenceAddress()),
            ccdSoleTrader.getPartyPhone(),
            representativeMapper.from(ccdSoleTrader),
            ccdSoleTrader.getPartyTitle(),
            ccdSoleTrader.getPartyBusinessName()
        );
    }
}
