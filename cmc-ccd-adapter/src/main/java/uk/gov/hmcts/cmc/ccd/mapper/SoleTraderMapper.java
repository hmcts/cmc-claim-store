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

        final CCDSoleTrader.CCDSoleTraderBuilder builder = CCDSoleTrader.builder();
        soleTrader.getTitle().ifPresent(builder::title);
        soleTrader.getMobilePhone().ifPresent(builder::mobilePhone);
        soleTrader.getBusinessName().ifPresent(builder::businessName);
        soleTrader.getCorrespondenceAddress()
            .ifPresent(address -> builder.correspondenceAddress(addressMapper.to(address)));
        soleTrader.getRepresentative()
            .ifPresent(representative -> builder.representative(representativeMapper.to(representative)));
        builder
            .name(soleTrader.getName())
            .address(addressMapper.to(soleTrader.getAddress()));

        return builder.build();
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
