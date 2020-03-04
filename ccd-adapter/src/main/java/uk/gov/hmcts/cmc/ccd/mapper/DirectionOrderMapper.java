package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
import uk.gov.hmcts.cmc.domain.models.Claim;
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

    public void from(CCDCase ccdCase, Claim.ClaimBuilder claimBuilder) {
        CCDDirectionOrder ccdDirectionOrder = ccdCase.getDirectionOrder();
        if (ccdDirectionOrder == null || ccdCase.getDirectionList().isEmpty()) {
            return;
        }

        DirectionOrder.DirectionOrderBuilder builder = DirectionOrder.builder();

        Optional.ofNullable(ccdCase.getEstimatedHearingDuration()).ifPresent(estimatedDuration ->
            builder.estimatedHearingDuration(HearingDurationType.valueOf(estimatedDuration.name())));

        Optional.ofNullable(ccdCase.getHearingCourt()).ifPresent(builder::hearingCourt);

        Optional.ofNullable(ccdCase.getPaperDetermination()).ifPresent(paperDetermination ->
            builder.paperDetermination(YesNoOption.valueOf(paperDetermination.name())));

        Optional.ofNullable(ccdCase.getGrantExpertReportPermission())
            .ifPresent(permission -> builder
                .grantExpertReportPermission(YesNoOption.valueOf(permission.name())));

        Optional.ofNullable(ccdCase.getExpertReportPermissionPartyAskedByClaimant())
            .ifPresent(permission -> builder
                .expertReportPermissionAskedByClaimant(YesNoOption.valueOf(permission.name())));

        Optional.ofNullable(ccdCase.getExpertReportPermissionPartyAskedByDefendant())
            .ifPresent(permission -> builder
                .expertReportPermissionAskedByDefendant(YesNoOption.valueOf(permission.name())));

        DirectionOrder directionOrder = builder
            .createdOn(ccdDirectionOrder.getCreatedOn())
            .hearingCourtName(ccdDirectionOrder.getHearingCourtName())
            .hearingCourtAddress(addressMapper.from(ccdDirectionOrder.getHearingCourtAddress()))
            .preferredDQCourt(ccdCase.getPreferredDQCourt())
            .preferredCourtObjectingReason(ccdCase.getPreferredCourtObjectingReason())
            .newRequestedCourt(ccdCase.getNewRequestedCourt())
            .extraDocUploadList(ccdCase.getExtraDocUploadList().stream()
                .map(CCDCollectionElement::getValue)
                .collect(Collectors.toList()))
            .expertReportInstruction(ccdCase.getExpertReportInstruction())
            .build();

        addUploadDocumentDirection(ccdCase, directionOrder);
        addEyeWitnessDirection(ccdCase, directionOrder);

        asStream(ccdCase.getOtherDirections())
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

        claimBuilder.directionOrder(directionOrder);
    }

    private void addEyeWitnessDirection(CCDCase ccdCase, DirectionOrder directionOrder) {
        if (ccdCase.getDirectionList().contains(CCDOrderDirectionType.EYEWITNESS)) {
            directionOrder.addDirection(mapDirectionData(ccdCase, CCDOrderDirectionType.EYEWITNESS));
        }
    }

    private void addUploadDocumentDirection(CCDCase ccdCase, DirectionOrder directionOrder) {
        if (ccdCase.getDirectionList().contains(CCDOrderDirectionType.DOCUMENTS)) {
            directionOrder.addDirection(mapDirectionData(ccdCase, CCDOrderDirectionType.DOCUMENTS));
        }
    }

    private Direction mapDirectionData(CCDCase ccdCase, CCDOrderDirectionType directionType) {
        Direction.DirectionBuilder builder = Direction.builder();
        mapDirectionForType(ccdCase, builder, directionType);
        return builder.build();
    }

    private void mapDirectionForType(
        CCDCase ccdCase,
        Direction.DirectionBuilder builder,
        CCDOrderDirectionType directionType
    ) {
        switch (directionType) {
            case DOCUMENTS:
                addDirectionParty(builder, ccdCase.getDocUploadForParty());

                builder.directionType(DirectionType.valueOf(directionType.name()))
                    .directionActionedDate(ccdCase.getDocUploadDeadline());
                break;
            case EYEWITNESS:
                addDirectionParty(builder, ccdCase.getEyewitnessUploadForParty());

                builder.directionType(DirectionType.valueOf(directionType.name()))
                    .directionActionedDate(ccdCase.getEyewitnessUploadDeadline());
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
