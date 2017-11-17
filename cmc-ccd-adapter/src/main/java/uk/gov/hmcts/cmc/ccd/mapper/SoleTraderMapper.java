package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDSoleTrader;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

@Component
public class SoleTraderMapper implements Mapper<CCDSoleTrader, SoleTrader> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public SoleTraderMapper(final AddressMapper addressMapper, final RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public CCDSoleTrader to(SoleTrader soleTrader) {
        return CCDSoleTrader.builder()
            .title(soleTrader.getTitle().orElse(null))
            .name(soleTrader.getName())
            .mobilePhone(soleTrader.getMobilePhone().orElse(null))
            .businessName(soleTrader.getBusinessName().orElse(null))
            .address(addressMapper.to(soleTrader.getAddress()))
            .correspondenceAddress(addressMapper.to(soleTrader.getCorrespondenceAddress().orElse(null)))
            .representative(representativeMapper.to(soleTrader.getRepresentative().orElse(null)))
            .build();
    }

    @Override
    public SoleTrader from(CCDSoleTrader ccdSoleTrader) {
        return new SoleTrader(
            ccdSoleTrader.getName(),
            addressMapper.from(ccdSoleTrader.getAddress()),
            addressMapper.from(ccdSoleTrader.getCorrespondenceAddress()),
            ccdSoleTrader.getMobilePhone(),
            representativeMapper.from(ccdSoleTrader.getRepresentative()),
            ccdSoleTrader.getTitle(),
            ccdSoleTrader.getBusinessName()
        );
    }
}
