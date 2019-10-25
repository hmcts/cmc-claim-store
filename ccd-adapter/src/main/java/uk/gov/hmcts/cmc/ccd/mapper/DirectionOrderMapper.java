package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt;
import uk.gov.hmcts.cmc.domain.models.orders.Direction;
import uk.gov.hmcts.cmc.domain.models.orders.DirectionHeaderType;
import uk.gov.hmcts.cmc.domain.models.orders.DirectionOrder;
import uk.gov.hmcts.cmc.domain.models.orders.DirectionParty;
import uk.gov.hmcts.cmc.domain.models.orders.DirectionType;
import uk.gov.hmcts.cmc.domain.models.orders.HearingDurationType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Objects;
import java.util.Optional;
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

        DirectionOrder.DirectionOrderBuilder builder = DirectionOrder.builder();

        Optional.ofNullable(directionOrderData.getEstimatedHearingDuration()).ifPresent(estimatedDuration ->
            builder.estimatedHearingDuration(HearingDurationType.valueOf(estimatedDuration.name())));

        Optional.ofNullable(directionOrderData.getHearingCourt()).ifPresent(hearingCourt ->
            builder.hearingCourt(PilotCourt.valueOf(hearingCourt.name())));

        Optional.ofNullable(directionOrderData.getPaperDetermination()).ifPresent(paperDetermination ->
            builder.paperDetermination(YesNoOption.valueOf(paperDetermination.name())));

        Optional.ofNullable(directionOrderData.getExpertReportPermissionPartyGivenToClaimant())
            .ifPresent(permission -> builder
                .expertReportPermissionGivenToClaimant(YesNoOption.valueOf(permission.name())));

        Optional.ofNullable(directionOrderData.getExpertReportPermissionPartyGivenToDefendant())
            .ifPresent(permission -> builder
                .expertReportPermissionGivenToDefendant(YesNoOption.valueOf(permission.name())));

        Optional.ofNullable(directionOrderData.getExpertReportPermissionPartyAskedByClaimant())
            .ifPresent(permission -> builder
                .expertReportPermissionAskedByClaimant(YesNoOption.valueOf(permission.name())));

        Optional.ofNullable(directionOrderData.getExpertReportPermissionPartyAskedByDefendant())
            .ifPresent(permission -> builder
                .expertReportPermissionAskedByDefendant(YesNoOption.valueOf(permission.name())));

        DirectionOrder directionOrder = builder
            .createdOn(ccdDirectionOrder.getCreatedOn())
            .hearingCourtName(ccdDirectionOrder.getHearingCourtName())
            .hearingCourtAddress(addressMapper.from(ccdDirectionOrder.getHearingCourtAddress()))
            .preferredDQCourt(directionOrderData.getPreferredDQCourt())
            .preferredCourtObjectingReason(directionOrderData.getPreferredCourtObjectingReason())
            .newRequestedCourt(directionOrderData.getNewRequestedCourt())
            .extraDocUploadList(directionOrderData.getExtraDocUploadList().stream()
                .map(CCDCollectionElement::getValue)
                .collect(Collectors.toList()))
            .expertReportInstructionsForClaimant(directionOrderData.getExpertReportInstructionClaimant()
                .stream().map(CCDCollectionElement::getValue).collect(Collectors.toList()))
            .expertReportInstructionsForDefendant(directionOrderData.getExpertReportInstructionDefendant()
                .stream().map(CCDCollectionElement::getValue).collect(Collectors.toList()))
            .build();

        addUploadDocumentDirection(directionOrderData, directionOrder);
        addEyeWitnessDirection(directionOrderData, directionOrder);

        asStream(directionOrderData.getOtherDirections())
            .map(CCDCollectionElement::getValue)
            .forEach(ccdOrderDirection -> directionOrder.addDirection(Direction.builder()
                .directionType(DirectionType.valueOf(ccdOrderDirection.getExtraOrderDirection().name()))
                .directionComment(ccdOrderDirection.getDirectionComment())
                .directionParty(Optional.ofNullable(ccdOrderDirection.getForParty())
                    .map(partyType -> DirectionParty.valueOf(partyType.name()))
                    .orElse(null))
                .directionActionedDate(ccdOrderDirection.getSendBy())
                .directionHeaderType(Optional.ofNullable(ccdOrderDirection.getOtherDirectionHeaders())
                    .map(headerType -> DirectionHeaderType.valueOf(headerType.name()))
                    .orElse(null))
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

    private void addEyeWitnessDirection(CCDOrderGenerationData directionOrderData, DirectionOrder directionOrder) {
        if (directionOrderData.getDirectionList().contains(CCDOrderDirectionType.EYEWITNESS)) {
            directionOrder.addDirection(mapDirectionData(directionOrderData, CCDOrderDirectionType.EYEWITNESS));
        }
    }

    private void addUploadDocumentDirection(CCDOrderGenerationData directionOrderData, DirectionOrder directionOrder) {
        if (directionOrderData.getDirectionList().contains(CCDOrderDirectionType.DOCUMENTS)) {
            directionOrder.addDirection(mapDirectionData(directionOrderData, CCDOrderDirectionType.DOCUMENTS));
        }
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
                addDirectionParty(builder, directionOrderData.getDocUploadForParty());

                builder.directionType(DirectionType.valueOf(directionType.name()))
                    .directionActionedDate(directionOrderData.getDocUploadDeadline());
                break;
            case EYEWITNESS:
                addDirectionParty(builder, directionOrderData.getEyewitnessUploadForParty());

                builder.directionType(DirectionType.valueOf(directionType.name()))
                    .directionActionedDate(directionOrderData.getEyewitnessUploadDeadline());
                break;
            default:
                throw new IllegalArgumentException("Invalid direction type");

        }
    }

    private void addDirectionParty(Direction.DirectionBuilder builder, CCDDirectionPartyType directionPartyType) {
        Optional.ofNullable(directionPartyType).ifPresent(party ->
            builder.directionParty(DirectionParty.valueOf(party.name())));
    }
}
