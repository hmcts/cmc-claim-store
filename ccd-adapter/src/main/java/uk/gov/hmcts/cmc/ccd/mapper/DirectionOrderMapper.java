package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirection;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.orders.Direction;
import uk.gov.hmcts.cmc.domain.orders.DirectionHeaderType;
import uk.gov.hmcts.cmc.domain.orders.DirectionOrder;
import uk.gov.hmcts.cmc.domain.orders.DirectionParty;
import uk.gov.hmcts.cmc.domain.orders.DirectionType;
import uk.gov.hmcts.cmc.domain.orders.HearingCourtType;
import uk.gov.hmcts.cmc.domain.orders.HearingDurationType;

import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

@Component
public class DirectionOrderMapper {

    private final AddressMapper addressMapper;

    public DirectionOrderMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public DirectionOrder from(CCDDirectionOrder ccdDirectionOrder, CCDOrderGenerationData directionOrderData) {
        if (ccdDirectionOrder == null || directionOrderData == null) {
            return null;
        }

        DirectionOrder directionOrder = DirectionOrder.builder()
            .createdOn(ccdDirectionOrder.getCreatedOn())
            .hearingCourtAddress(addressMapper.from(ccdDirectionOrder.getHearingCourtAddress()))
            .estimatedHearingDuration(HearingDurationType
                .valueOf(directionOrderData.getEstimatedHearingDuration().name()))
            .hearingCourt(HearingCourtType.valueOf(directionOrderData.getHearingCourt().name()))
            .paperDetermination(YesNoOption.fromValue(directionOrderData.getPaperDetermination().name()))
            .hearingCourt(HearingCourtType.valueOf(directionOrderData.getHearingCourt().name()))
            .preferredDQCourt(directionOrderData.getPreferredDQCourt())
            .preferredCourtObjectingReason(directionOrderData.getPreferredCourtObjectingReason())
            .newRequestedCourt(directionOrderData.getNewRequestedCourt())
            .extraDocUploadList(directionOrderData.getExtraDocUploadList().stream()
                .map(CCDCollectionElement::getValue)
                .collect(Collectors.toList()))
            .build();

        addUploadDocumentDirection(directionOrderData, directionOrder);
        addEyeWitnessDirection(directionOrderData, directionOrder);

        asStream(directionOrderData.getOtherDirections())
            .map(CCDCollectionElement::getValue)
            .forEach(ccdOrderDirection -> directionOrder.addDirection(Direction.builder()
                .directionType(DirectionType.valueOf(ccdOrderDirection.getExtraOrderDirection().name()))
                .directionComment(ccdOrderDirection.getDirectionComment())
                .directionParty(DirectionParty.valueOf(ccdOrderDirection.getForParty().name()))
                .directionActionedBy(ccdOrderDirection.getSendBy())
                .directionHeaderType(getDirectionHeaderType(ccdOrderDirection))
                .expertReports(asStream(ccdOrderDirection.getExpertReports())
                    .filter(Objects::nonNull)
                    .map(CCDCollectionElement::getValue)
                    .collect(Collectors.toList()))
                .extraDocuments(asStream(ccdOrderDirection.getExtraDocUploadList())
                    .map(CCDCollectionElement::getValue)
                    .collect(Collectors.toList()))
                .build()));

        return directionOrder;
    }

    private DirectionHeaderType getDirectionHeaderType(CCDOrderDirection ccdOrderDirection) {
        return ccdOrderDirection.getOtherDirectionHeaders() == null
            ? null
            : DirectionHeaderType.valueOf(ccdOrderDirection.getOtherDirectionHeaders().name());
    }

    private void addEyeWitnessDirection(CCDOrderGenerationData directionOrderData, DirectionOrder directionOrder) {
        directionOrder.addDirection(mapDirectionData(directionOrderData, CCDOrderDirectionType.EYEWITNESS));
    }

    private void addUploadDocumentDirection(CCDOrderGenerationData directionOrderData, DirectionOrder directionOrder) {
        directionOrder.addDirection(mapDirectionData(directionOrderData, CCDOrderDirectionType.DOCUMENTS));
    }

    private Direction mapDirectionData(CCDOrderGenerationData directionOrderData, CCDOrderDirectionType directionType) {
        Direction.DirectionBuilder builder = Direction.builder();
        mapDirectionForType(directionOrderData, builder, directionType);
        return builder.build();
    }

    private void mapDirectionForType(
        CCDOrderGenerationData directionOrderData,
        Direction.DirectionBuilder builder,
        CCDOrderDirectionType directionType
    ) {
        switch (directionType) {
            case DOCUMENTS:
                builder.directionType(DirectionType.valueOf(directionType.name()))
                    .directionParty(DirectionParty.valueOf(directionOrderData.getDocUploadForParty().name()))
                    .directionActionedBy(directionOrderData.getDocUploadDeadline());
                return;
            case EYEWITNESS:
                builder.directionType(DirectionType.valueOf(directionType.name()))
                    .directionParty(DirectionParty.valueOf(directionOrderData.getEyewitnessUploadForParty().name()))
                    .directionActionedBy(directionOrderData.getEyewitnessUploadDeadline());
                return;
            default:
                throw new IllegalArgumentException("Invalid direction type");

        }
    }
}
