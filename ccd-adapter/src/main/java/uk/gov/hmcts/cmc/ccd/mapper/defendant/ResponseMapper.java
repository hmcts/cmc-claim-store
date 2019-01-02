package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefenceType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.ccd.mapper.EvidenceRowMapper;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.ccd.mapper.TimelineEventMapper;
import uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans.StatementOfMeansMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isAllBlank;
import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;

@Component
public class ResponseMapper {
    private final EvidenceRowMapper evidenceRowMapper;
    private final TimelineEventMapper timelineEventMapper;
    private final DefendantPartyMapper defendantPartyMapper;
    private final PaymentIntentionMapper paymentIntentionMapper;
    private final StatementOfMeansMapper statementOfMeansMapper;

    public ResponseMapper(
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

    public void to(CCDDefendant.CCDDefendantBuilder builder, Response response) {
        requireNonNull(builder, "builder must not be null");
        requireNonNull(response, "response must not be null");

        builder.responseFreeMediationOption(
            CCDYesNoOption.valueOf(response.getFreeMediation().orElse(NO).name())
        );

        builder.responseMoreTimeNeededOption(CCDYesNoOption.valueOf(response.getMoreTimeNeeded().orElse(NO).name()));

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
            mapPaymentDeclaration(builder)
        );
        builder.responseDefence(response.getDefence());
        response.getEvidence().ifPresent(mapDefendantEvidence(builder));
        response.getTimeline().ifPresent(mapDefendantTimeline(builder));
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

    private void toFullDefenceResponse(
        CCDDefendant.CCDDefendantBuilder builder,
        FullDefenceResponse fullDefenceResponse
    ) {

        builder.responseDefenceType(
            CCDDefenceType.valueOf(fullDefenceResponse.getDefenceType().name())
        );
        builder.responseDefence(fullDefenceResponse.getDefence().orElse(EMPTY));
        fullDefenceResponse.getPaymentDeclaration().ifPresent(mapPaymentDeclaration(builder));
        fullDefenceResponse.getEvidence().ifPresent(mapDefendantEvidence(builder));
        fullDefenceResponse.getTimeline().ifPresent(mapDefendantTimeline(builder));
    }

    private Consumer<DefendantTimeline> mapDefendantTimeline(CCDDefendant.CCDDefendantBuilder builder) {
        return timeline -> {
            builder.defendantTimeLineComment(timeline.getComment().orElse(EMPTY));
            builder.defendantTimeLineEvents(timeline.getEvents().stream()
                .map(timelineEventMapper::to)
                .filter(Objects::nonNull)
                .map(event -> CCDCollectionElement.<CCDTimelineEvent>builder().value(event).build())
                .collect(Collectors.toList())
            );
        };
    }

    private Consumer<DefendantEvidence> mapDefendantEvidence(CCDDefendant.CCDDefendantBuilder builder) {
        return evidence -> {
            builder.responseEvidenceComment(evidence.getComment().orElse(EMPTY));
            builder.responseEvidenceRows(evidence.getRows().stream()
                .map(evidenceRowMapper::to)
                .filter(Objects::nonNull)
                .map(row -> CCDCollectionElement.<CCDEvidenceRow>builder().value(row).build())
                .collect(Collectors.toList())
            );
        };
    }

    private Consumer<PaymentDeclaration> mapPaymentDeclaration(CCDDefendant.CCDDefendantBuilder builder) {
        return paymentDeclaration -> {
            builder.paymentDeclarationExplanation(paymentDeclaration.getExplanation());
            builder.paymentDeclarationPaidDate(paymentDeclaration.getPaidDate());
        };
    }

    public void from(Claim.ClaimBuilder claimBuilder, CCDDefendant defendant) {
        requireNonNull(claimBuilder, "claimBuilder must not be null");
        requireNonNull(defendant, "defendant must not be null");
        if (defendant.getResponseType() == null) {
            return;
        }

        switch (defendant.getResponseType()) {
            case FULL_DEFENCE:
                claimBuilder.response(extractFullDefence(defendant));
                break;
            case FULL_ADMISSION:
                claimBuilder.response(extractFullAdmission(defendant));
                break;
            case PART_ADMISSION:
                claimBuilder.response(extractPartAdmission(defendant));
                break;
            default:
                throw new MappingException("Invalid responseType");
        }
    }

    private FullDefenceResponse extractFullDefence(CCDDefendant defendant) {
        return FullDefenceResponse.builder()
            .defendant(defendantPartyMapper.from(defendant))
            .statementOfTruth(extractStatementOfTruth(defendant))
            .moreTimeNeeded(YesNoOption.valueOf(defendant.getResponseMoreTimeNeededOption().name()))
            .freeMediation(YesNoOption.valueOf(defendant.getResponseMoreTimeNeededOption().name()))
            .defenceType(DefenceType.valueOf(defendant.getResponseDefenceType().name()))
            .defence(defendant.getResponseDefence())
            .evidence(extractDefendantEvidence(defendant))
            .timeline(extractDefendantTimeline(defendant))
            .paymentDeclaration(extractPaymentDeclaration(defendant))
            .build();
    }

    private StatementOfTruth extractStatementOfTruth(CCDDefendant defendant) {
        String signerName = defendant.getResponseDefendantSOTSignerName();
        String signerRole = defendant.getResponseDefendantSOTSignerRole();
        if (isAllBlank(signerName, signerRole)) {
            return null;
        } else {
            return new StatementOfTruth(signerName, signerRole);
        }
    }

    private PaymentDeclaration extractPaymentDeclaration(CCDDefendant defendant) {
        LocalDate paidDate = defendant.getPaymentDeclarationPaidDate();
        String explanation = defendant.getPaymentDeclarationExplanation();
        if (paidDate == null && explanation == null) {
            return null;
        } else {
            return new PaymentDeclaration(paidDate, explanation);
        }
    }

    private DefendantTimeline extractDefendantTimeline(CCDDefendant defendant) {
        return new DefendantTimeline(
            asStream(defendant.getDefendantTimeLineEvents())
                .map(CCDCollectionElement::getValue)
                .map(timelineEventMapper::from)
                .collect(Collectors.toList()),
            defendant.getDefendantTimeLineComment()
        );
    }

    private DefendantEvidence extractDefendantEvidence(CCDDefendant defendant) {
        return new DefendantEvidence(
            asStream(defendant.getResponseEvidenceRows())
                .map(CCDCollectionElement::getValue)
                .map(evidenceRowMapper::from)
                .collect(Collectors.toList()),
            defendant.getResponseEvidenceComment()
        );
    }

    private PartAdmissionResponse extractPartAdmission(CCDDefendant defendant) {
        return PartAdmissionResponse.builder()
            .defendant(defendantPartyMapper.from(defendant))
            .statementOfTruth(extractStatementOfTruth(defendant))
            .moreTimeNeeded(YesNoOption.valueOf(defendant.getResponseMoreTimeNeededOption().name()))
            .freeMediation(YesNoOption.valueOf(defendant.getResponseMoreTimeNeededOption().name()))
            .amount(defendant.getResponseAmount())
            .paymentDeclaration(extractPaymentDeclaration(defendant))
            .paymentIntention(paymentIntentionMapper.from(defendant.getDefendantPaymentIntention()))
            .defence(defendant.getResponseDefence())
            .evidence(extractDefendantEvidence(defendant))
            .timeline(extractDefendantTimeline(defendant))
            .statementOfMeans(statementOfMeansMapper.from(defendant.getStatementOfMeans()))
            .build();
    }

    private FullAdmissionResponse extractFullAdmission(CCDDefendant defendant) {
        return FullAdmissionResponse.builder()
            .defendant(defendantPartyMapper.from(defendant))
            .statementOfTruth(extractStatementOfTruth(defendant))
            .moreTimeNeeded(YesNoOption.valueOf(defendant.getResponseMoreTimeNeededOption().name()))
            .freeMediation(YesNoOption.valueOf(defendant.getResponseMoreTimeNeededOption().name()))
            .paymentIntention(paymentIntentionMapper.from(defendant.getDefendantPaymentIntention()))
            .statementOfMeans(statementOfMeansMapper.from(defendant.getStatementOfMeans()))
            .build();
    }
}
