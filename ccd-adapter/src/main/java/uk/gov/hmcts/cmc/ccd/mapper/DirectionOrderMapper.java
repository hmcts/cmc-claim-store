package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.domain.models.DirectionOrder;

@Component
public class DirectionOrderMapper implements Mapper<CCDDirectionOrder, DirectionOrder> {

    private final AddressMapper addressMapper;

    public DirectionOrderMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public CCDDirectionOrder to(DirectionOrder directionOrder) {
        if (directionOrder == null) {
            return null;
        }

        return CCDDirectionOrder.builder()
            .createdOn(directionOrder.getCreatedOn())
            .hearingCourtAddress(addressMapper.to(directionOrder.getHearingCourtAddress()))
            .build();
    }

    @Override
    public DirectionOrder from(CCDDirectionOrder ccdDirectionOrder) {
        if (ccdDirectionOrder == null) {
            return null;
        }

        return DirectionOrder.builder()
            .createdOn(ccdDirectionOrder.getCreatedOn())
            .hearingCourtAddress(addressMapper.from(ccdDirectionOrder.getHearingCourtAddress()))
            .build();
    }
}
