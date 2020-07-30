package uk.gov.hmcts.cmc.ccd.mapper;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDChannelType;
import uk.gov.hmcts.cmc.ccd.domain.CCDProceedOnPaperReasonType;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.util.MapperUtil;
import uk.gov.hmcts.cmc.domain.models.ChannelType;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ProceedOfflineReasonType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.utils.MonetaryConversions;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.mapper.ClaimSubmissionOperationIndicatorMapper.mapClaimSubmissionOperationIndicatorsToCCD;
import static uk.gov.hmcts.cmc.ccd.mapper.ClaimSubmissionOperationIndicatorMapper.mapFromCCDClaimSubmissionOperationIndicators;
import static uk.gov.hmcts.cmc.ccd.util.MapperUtil.getMediationOutcome;
import static uk.gov.hmcts.cmc.ccd.util.MapperUtil.toCaseName;
import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

@Component
public class CaseMapper {

    private final ClaimMapper claimMapper;
    private final boolean isMigrated;
    private final ClaimDocumentCollectionMapper claimDocumentCollectionMapper;
    private final ReviewOrderMapper reviewOrderMapper;
    private final DirectionOrderMapper directionOrderMapper;
    private final TransferContentMapper transferContentMapper;
    private final BulkPrintDetailsMapper bulkPrintDetailsMapper;

    public CaseMapper(
        ClaimMapper claimMapper,
        @Value("${migration.cases.flag:false}") boolean isMigrated,
        ClaimDocumentCollectionMapper claimDocumentCollectionMapper,
        ReviewOrderMapper reviewOrderMapper,
        DirectionOrderMapper directionOrderMapper,
        TransferContentMapper transferContentMapper,
        BulkPrintDetailsMapper bulkPrintDetailsMapper
    ) {
        this.claimMapper = claimMapper;
        this.isMigrated = isMigrated;
        this.claimDocumentCollectionMapper = claimDocumentCollectionMapper;
        this.reviewOrderMapper = reviewOrderMapper;
        this.directionOrderMapper = directionOrderMapper;
        this.transferContentMapper = transferContentMapper;
        this.bulkPrintDetailsMapper = bulkPrintDetailsMapper;
    }

    public CCDCase to(Claim claim) {
        final CCDCase.CCDCaseBuilder builder = CCDCase.builder();

        claimMapper.to(claim, builder);

        claim.getClaimDocumentCollection()
            .ifPresent(claimDocumentCollection -> claimDocumentCollectionMapper.to(claimDocumentCollection, builder));

        claim.getReviewOrder()
            .map(reviewOrderMapper::to)
            .ifPresent(builder::reviewOrder);

        claim.getChannel()
            .map(ChannelType::name)
            .map(CCDChannelType::valueOf)
            .ifPresent(builder::channel);

        claim.getDateReferredForDirections().ifPresent(builder::dateReferredForDirections);
        claim.getPreferredDQCourt().ifPresent(builder::preferredDQCourt);
        claim.getProceedOfflineReason()
            .map(ProceedOfflineReasonType::name)
            .map(CCDProceedOnPaperReasonType::valueOf)
            .ifPresent(builder::proceedOnPaperReason);

        builder.bulkPrintDetails(asStream(claim.getBulkPrintDetails())
            .map(bulkPrintDetailsMapper::to)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        );

        return builder
            .id(claim.getId())
            .externalId(claim.getExternalId())
            .previousServiceCaseReference(claim.getReferenceNumber())
            .submitterId(claim.getSubmitterId())
            .submitterEmail(claim.getSubmitterEmail())
            .issuedOn(claim.getIssuedOn())
            .currentInterestAmount(
                claim.getTotalInterestTillDateOfIssue()
                    .map(interest -> String.valueOf(MonetaryConversions.poundsToPennies(interest)))
                    .orElse(null))
            .submittedOn(claim.getCreatedAt())
            .features(claim.getFeatures() != null ? String.join(",", claim.getFeatures()) : null)
            .migratedFromClaimStore(isMigrated ? YES : NO)
            .caseName(toCaseName.apply(claim))
            .claimSubmissionOperationIndicators(
                mapClaimSubmissionOperationIndicatorsToCCD.apply(claim.getClaimSubmissionOperationIndicators()))
            .intentionToProceedDeadline(claim.getIntentionToProceedDeadline())
            .proceedOnPaperOtherReason(claim.getProceedOfflineOtherReasonDescription())
            .evidenceHandled(convertYesNo(claim.getEvidenceHandled()))
            .build();
    }

    public Claim from(CCDCase ccdCase) {
        Claim.ClaimBuilder builder = Claim.builder();
        claimMapper.from(ccdCase, builder);

        claimDocumentCollectionMapper.from(ccdCase, builder);
        directionOrderMapper.from(ccdCase, builder);

        builder
            .id(ccdCase.getId())
            .state(EnumUtils.getEnumIgnoreCase(ClaimState.class, ccdCase.getState()))
            .ccdCaseId(ccdCase.getId())
            .submitterId(ccdCase.getSubmitterId())
            .externalId(ccdCase.getExternalId())
            .referenceNumber(ccdCase.getPreviousServiceCaseReference())
            .createdAt(ccdCase.getSubmittedOn())
            .issuedOn(ccdCase.getIssuedOn())
            .submitterEmail(ccdCase.getSubmitterEmail())
            .state(ClaimState.fromValue(ccdCase.getState()))
            .claimSubmissionOperationIndicators(
                mapFromCCDClaimSubmissionOperationIndicators.apply(ccdCase.getClaimSubmissionOperationIndicators()))
            .intentionToProceedDeadline(ccdCase.getIntentionToProceedDeadline())
            .reviewOrder(reviewOrderMapper.from(ccdCase.getReviewOrder()))
            .dateReferredForDirections(ccdCase.getDateReferredForDirections())
            .paperResponse(MapperUtil.hasPaperResponse.apply(ccdCase))
            .evidenceHandled(convertCCDYesNo(ccdCase.getEvidenceHandled()))
            .proceedOfflineOtherReasonDescription(ccdCase.getProceedOnPaperOtherReason())
            .mediationOutcome(getMediationOutcome(ccdCase))
            .transferContent(transferContentMapper.from(ccdCase.getTransferContent()));

        Optional.ofNullable(ccdCase.getProceedOnPaperReason())
            .map(CCDProceedOnPaperReasonType::name)
            .map(ProceedOfflineReasonType::valueOf)
            .ifPresent(builder::proceedOfflineReason);

        if (ccdCase.getFeatures() != null) {
            builder.features(Arrays.asList(ccdCase.getFeatures().split(",")));
        }

        if (ccdCase.getChannel() != null) {
            builder.channel(ChannelType.valueOf(ccdCase.getChannel().name()));
        }

        if (ccdCase.getPreferredDQCourt() != null) {
            builder.preferredDQCourt(ccdCase.getPreferredDQCourt());
        }

        builder.bulkPrintDetails(asStream(ccdCase.getBulkPrintDetails())
            .map(bulkPrintDetailsMapper::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        );

        return builder.build();
    }

    private YesNoOption convertCCDYesNo(CCDYesNoOption ccdYesNoOption) {
        return Optional.ofNullable(ccdYesNoOption)
            .map(e -> EnumUtils.getEnumIgnoreCase(YesNoOption.class, e.name()))
            .orElse(null);
    }

    private CCDYesNoOption convertYesNo(YesNoOption ccdYesNoOption) {
        return Optional.ofNullable(ccdYesNoOption)
            .map(e -> EnumUtils.getEnumIgnoreCase(CCDYesNoOption.class, e.name()))
            .orElse(null);
    }
}
