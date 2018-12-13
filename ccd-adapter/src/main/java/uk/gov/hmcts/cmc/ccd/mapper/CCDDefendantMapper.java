package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDDefenceType;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Component
public class CCDDefendantMapper {

    private final EvidenceRowMapper evidenceRowMapper;
    private final TimelineEventMapper timelineEventMapper;

    @Autowired
    public CCDDefendantMapper(EvidenceRowMapper evidenceRowMapper, TimelineEventMapper timelineEventMapper) {
        this.evidenceRowMapper = evidenceRowMapper;
        this.timelineEventMapper = timelineEventMapper;
    }

    public CCDDefendant to(Claim claim) {
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        builder.responseDeadline(claim.getResponseDeadline());
        builder.responseSubmittedDateTime(claim.getRespondedAt());

        claim.getResponse().ifPresent(
            response -> {
                builder.responseFreeMediationOption(
                    CCDYesNoOption.valueOf(response.getFreeMediation().orElse(YesNoOption.NO).name())
                );

                builder.responseMoreTimeNeededOption(CCDYesNoOption.valueOf(response.getMoreTimeNeeded().name()));

                response.getStatementOfTruth().ifPresent(
                    statementOfTruth -> {
                        builder.responseDefendantSOTSignerName(statementOfTruth.getSignerName());
                        builder.responseDefendantSOTSignerRole(statementOfTruth.getSignerRole());
                    }
                );
                // PartyMapper
                switch (response.getResponseType()) {
                    case FULL_DEFENCE:
                        toFullDefenceResponse(builder, (FullDefenceResponse) response);
                        break;
                    case FULL_ADMISSION:
                        toFullAdmissionResponse(builder, (FullAdmissionResponse) response);
                        break;
                    case PART_ADMISSION:
                        toPartAdmissionResponse(builder, (PartAdmissionResponse)response);
                        break;
                }
//                builder.courtDetermination();
            }
        );

        return builder.build();
    }

    private void toPartAdmissionResponse(CCDDefendant.CCDDefendantBuilder builder, PartAdmissionResponse response) {
    }

    private void toFullAdmissionResponse(CCDDefendant.CCDDefendantBuilder builder, FullAdmissionResponse response) {
    }

    private void toFullDefenceResponse(CCDDefendant.CCDDefendantBuilder builder, FullDefenceResponse response) {
        FullDefenceResponse fullDefenceResponse = response;
        builder.responseDefenceType(
            CCDDefenceType.valueOf(fullDefenceResponse.getDefenceType().name())
        );
        builder.responseDefence(fullDefenceResponse.getDefence().orElse(EMPTY));
        fullDefenceResponse.getPaymentDeclaration().ifPresent(
            paymentDeclaration -> {
                builder.paymentDeclarationExplanation(paymentDeclaration.getExplanation());
                builder.paymentDeclarationPaidDate(paymentDeclaration.getPaidDate());
            }
        );
        fullDefenceResponse.getEvidence().ifPresent(defendantEvidence -> {
            builder.responseEvidenceComment(defendantEvidence.getComment().orElse(EMPTY));
            builder.responseEvidenceRows(
                defendantEvidence.getRows()
                    .stream()
                    .map(evidenceRowMapper::to)
                    .filter(Objects::nonNull)
                    .map(row -> CCDCollectionElement.<CCDEvidenceRow>builder().value(row).build())
                    .collect(Collectors.toList())
            );

        });
        fullDefenceResponse.getTimeline().ifPresent(defendantTimeline -> {
            builder.defendantTimeLineComment(defendantTimeline.getComment().orElse(EMPTY));
            builder.defendantTimeLineEvents(
                defendantTimeline.getEvents()
                    .stream()
                    .map(timelineEventMapper::to)
                    .filter(Objects::nonNull)
                    .map(event -> CCDCollectionElement.<CCDTimelineEvent>builder().value(event).build())
                    .collect(Collectors.toList())
            );
        });
    }

}
