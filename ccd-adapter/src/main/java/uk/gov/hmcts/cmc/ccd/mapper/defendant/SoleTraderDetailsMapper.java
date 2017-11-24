package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDSoleTrader;
import uk.gov.hmcts.cmc.ccd.mapper.AddressMapper;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.RepresentativeMapper;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;

@Component
public class SoleTraderDetailsMapper implements Mapper<CCDSoleTrader, SoleTraderDetails> {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public SoleTraderDetailsMapper(final AddressMapper addressMapper, final RepresentativeMapper representativeMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public CCDSoleTrader to(SoleTraderDetails soleTrader) {

        final CCDSoleTrader.CCDSoleTraderBuilder builder = CCDSoleTrader.builder();
        soleTrader.getTitle().ifPresent(builder::title);
        soleTrader.getEmail().ifPresent(builder::email);
        soleTrader.getBusinessName().ifPresent(builder::businessName);
        soleTrader.getServiceAddress()
            .ifPresent(address -> builder.correspondenceAddress(addressMapper.to(address)));
        soleTrader.getRepresentative()
            .ifPresent(representative -> builder.representative(representativeMapper.to(representative)));
        builder
            .name(soleTrader.getName())
            .address(addressMapper.to(soleTrader.getAddress()));

        return builder.build();
    }

    @Override
    public SoleTraderDetails from(CCDSoleTrader ccdSoleTrader) {
        return new SoleTraderDetails(
            ccdSoleTrader.getName(),
            addressMapper.from(ccdSoleTrader.getAddress()),
            ccdSoleTrader.getEmail(),
            representativeMapper.from(ccdSoleTrader.getRepresentative()),
            addressMapper.from(ccdSoleTrader.getCorrespondenceAddress()),
            ccdSoleTrader.getTitle(),
            ccdSoleTrader.getBusinessName()
        );
    }
}
