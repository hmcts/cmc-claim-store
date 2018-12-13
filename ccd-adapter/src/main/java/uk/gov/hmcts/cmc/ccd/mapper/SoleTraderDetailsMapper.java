package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;

@Component
public class SoleTraderDetailsMapper
    implements BuilderMapper<CCDDefendant, SoleTraderDetails, CCDDefendant.CCDDefendantBuilder> {

    private final AddressMapper addressMapper;
    private final DefendantRepresentativeMapper representativeMapper;

    @Autowired
    public SoleTraderDetailsMapper(AddressMapper addressMapper, DefendantRepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public void to(SoleTraderDetails soleTrader, CCDDefendant.CCDDefendantBuilder builder) {

        soleTrader.getTitle().ifPresent(builder::partyTitle);
        soleTrader.getBusinessName().ifPresent(builder::partyBusinessName);
        soleTrader.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        builder
            .partyName(soleTrader.getName())
            .partyAddress(addressMapper.to(soleTrader.getAddress()));

    }

    @Override
    public SoleTraderDetails from(CCDDefendant ccdSoleTrader) {
        return new SoleTraderDetails(
            ccdSoleTrader.getPartyName(),
            addressMapper.from(ccdSoleTrader.getPartyAddress()),
            ccdSoleTrader.getPartyEmail(),
            representativeMapper.from(ccdSoleTrader),
            addressMapper.from(ccdSoleTrader.getPartyServiceAddress()),
            ccdSoleTrader.getPartyPhone(),
            ccdSoleTrader.getPartyTitle()
        );
    }
}
