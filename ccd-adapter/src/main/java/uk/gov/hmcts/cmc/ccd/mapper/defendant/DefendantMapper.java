package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefenceType;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.ccd.mapper.EvidenceRowMapper;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.ccd.mapper.TimelineEventMapper;
import uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans.StatementOfMeansMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Component("ccdDefendantMapper")
public class DefendantMapper {

    private final EvidenceRowMapper evidenceRowMapper;
    private final TimelineEventMapper timelineEventMapper;
    private final DefendantPartyMapper defendantPartyMapper;
    private final PaymentIntentionMapper paymentIntentionMapper;
    private final StatementOfMeansMapper statementOfMeansMapper;

    @Autowired
    public DefendantMapper(
        EvidenceRowMapper evidenceRowMapper,
        TimelineEventMapper timelineEventMapper,
        DefendantPartyMapper defendantPartyMapper,
        PaymentIntentionMapper paymentIntentionMapper,
        StatementOfMeansMapper statementOfMeansMapper
    ) {
        this.evidenceRowMapper = evidenceRowMapper;
        this.timelineEventMapper = timelineEventMapper;
        this.defendantPartyMapper = defendantPartyMapper;
        this.paymentIntentionMapper = paymentIntentionMapper;
        this.statementOfMeansMapper = statementOfMeansMapper;
    }

    public void to(CCDDefendant.CCDDefendantBuilder builder, Claim claim) {

        builder.responseDeadline(claim.getResponseDeadline());
        builder.responseSubmittedDateTime(claim.getRespondedAt());

        claim.getResponse().ifPresent(response -> toResponse(builder, response));
    }

    private void toResponse(CCDDefendant.CCDDefendantBuilder builder, Response response) {

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
        defendantPartyMapper.to(builder, response.getDefendant());

        switch (response.getResponseType()) {
            case FULL_DEFENCE:
                toFullDefenceResponse(builder, (FullDefenceResponse) response);
                break;
            case FULL_ADMISSION:
                toFullAdmissionResponse(builder, (FullAdmissionResponse) response);
                break;
            case PART_ADMISSION:
                toPartAdmissionResponse(builder, (PartAdmissionResponse) response);
                break;
            default:
                throw new MappingException("Invalid response type " + response.getResponseType());
        }
    }

    private void toPartAdmissionResponse(CCDDefendant.CCDDefendantBuilder builder, PartAdmissionResponse response) {

        builder.responseAmount(response.getAmount());
        response.getPaymentDeclaration().ifPresent(
            paymentDeclarationConsumer(builder)
        );
        builder.responseDefence(response.getDefence());
        response.getEvidence().ifPresent(defendantEvidenceConsumer(builder));
        response.getTimeline().ifPresent(defendantTimelineConsumer(builder));
        response.getPaymentIntention().ifPresent(
            paymentIntention -> builder.defendantPaymentIntention(paymentIntentionMapper.to(paymentIntention))
        );

        response.getStatementOfMeans().ifPresent(
            statementOfMeans -> builder.statementOfMeans(statementOfMeansMapper.to(statementOfMeans))
        );
    }

    private void toFullAdmissionResponse(CCDDefendant.CCDDefendantBuilder builder, FullAdmissionResponse response) {

        builder.defendantPaymentIntention(paymentIntentionMapper.to(response.getPaymentIntention()));

        response.getStatementOfMeans().ifPresent(
            statementOfMeans -> builder.statementOfMeans(statementOfMeansMapper.to(statementOfMeans))
        );
    }

    private void toFullDefenceResponse(CCDDefendant.CCDDefendantBuilder builder, FullDefenceResponse response) {

        FullDefenceResponse fullDefenceResponse = response;
        builder.responseDefenceType(
            CCDDefenceType.valueOf(fullDefenceResponse.getDefenceType().name())
        );
        builder.responseDefence(fullDefenceResponse.getDefence().orElse(EMPTY));
        fullDefenceResponse.getPaymentDeclaration().ifPresent(paymentDeclarationConsumer(builder));
        fullDefenceResponse.getEvidence().ifPresent(defendantEvidenceConsumer(builder));
        fullDefenceResponse.getTimeline().ifPresent(defendantTimelineConsumer(builder));
    }

    private Consumer<DefendantTimeline> defendantTimelineConsumer(CCDDefendant.CCDDefendantBuilder builder) {
        return timeline -> {
            builder.defendantTimeLineComment(timeline.getComment().orElse(EMPTY));
            builder.defendantTimeLineEvents(
                timeline.getEvents()
                    .stream()
                    .map(timelineEventMapper::to)
                    .filter(Objects::nonNull)
                    .map(event -> CCDCollectionElement.<CCDTimelineEvent>builder().value(event).build())
                    .collect(Collectors.toList())
            );
        };
    }

    private Consumer<DefendantEvidence> defendantEvidenceConsumer(CCDDefendant.CCDDefendantBuilder builder) {
        return evidence -> {
            builder.responseEvidenceComment(evidence.getComment().orElse(EMPTY));
            builder.responseEvidenceRows(
                evidence.getRows()
                    .stream()
                    .map(evidenceRowMapper::to)
                    .filter(Objects::nonNull)
                    .map(row -> CCDCollectionElement.<CCDEvidenceRow>builder().value(row).build())
                    .collect(Collectors.toList())
            );
        };
    }

    private Consumer<PaymentDeclaration> paymentDeclarationConsumer(CCDDefendant.CCDDefendantBuilder builder) {
        return paymentDeclaration -> {
            builder.paymentDeclarationExplanation(paymentDeclaration.getExplanation());
            builder.paymentDeclarationPaidDate(paymentDeclaration.getPaidDate());
        };
    }

    public void from(CCDDefendant.CCDDefendantBuilder builder, Claim.ClaimBuilder claimBuilder) {

    }
}
