package uk.gov.hmcts.cmc.ccd.deprecated.mapper.response;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDPartAdmissionResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDPaymentDeclaration;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDPaymentIntention;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.DefendantEvidenceMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.PartyMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.PaymentDeclarationMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.StatementOfTruthMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans.StatementOfMeansMapper;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Optional;

//@Component
public class PartAdmissionResponseMapper implements Mapper<CCDPartAdmissionResponse, PartAdmissionResponse> {

    private final PartyMapper partyMapper;
    private final PaymentIntentionMapper paymentIntentionMapper;
    private final StatementOfMeansMapper statementOfMeansMapper;
    private final PaymentDeclarationMapper paymentDeclarationMapper;
    private final DefendantTimelineMapper timelineMapper;
    private final DefendantEvidenceMapper evidenceMapper;
    private final StatementOfTruthMapper statementOfTruthMapper;

    @Autowired
    public PartAdmissionResponseMapper(
        PartyMapper partyMapper,
        PaymentIntentionMapper paymentIntentionMapper,
        StatementOfMeansMapper statementOfMeansMapper,
        PaymentDeclarationMapper paymentDeclarationMapper,
        DefendantTimelineMapper timelineMapper,
        DefendantEvidenceMapper evidenceMapper,
        StatementOfTruthMapper statementOfTruthMapper
    ) {
        this.partyMapper = partyMapper;
        this.paymentIntentionMapper = paymentIntentionMapper;
        this.statementOfMeansMapper = statementOfMeansMapper;
        this.paymentDeclarationMapper = paymentDeclarationMapper;
        this.timelineMapper = timelineMapper;
        this.evidenceMapper = evidenceMapper;
        this.statementOfTruthMapper = statementOfTruthMapper;
    }

    @Override
    public CCDPartAdmissionResponse to(PartAdmissionResponse partAdmissionResponse) {
        CCDPartAdmissionResponse.CCDPartAdmissionResponseBuilder builder = CCDPartAdmissionResponse.builder()
            .freeMediationOption(CCDYesNoOption.valueOf(
                partAdmissionResponse.getFreeMediation().orElse(YesNoOption.NO).name())
            )
            .defendant(partyMapper.to(partAdmissionResponse.getDefendant()))
            .defence(partAdmissionResponse.getDefence())
            .amount(partAdmissionResponse.getAmount());

        partAdmissionResponse.getPaymentIntention()
            .ifPresent(paymentIntention -> builder.paymentIntention(paymentIntentionMapper.to(paymentIntention)));

        if (partAdmissionResponse.getMoreTimeNeeded() != null) {
            builder.moreTimeNeededOption(CCDYesNoOption.valueOf(partAdmissionResponse.getMoreTimeNeeded().name()));
        }

        partAdmissionResponse.getStatementOfMeans()
            .ifPresent(statementOfMeans -> builder.statementOfMeans(statementOfMeansMapper.to(statementOfMeans)));

        partAdmissionResponse.getPaymentDeclaration().ifPresent(paymentDeclaration ->
            builder.paymentDeclaration(paymentDeclarationMapper.to(paymentDeclaration)));

        partAdmissionResponse.getTimeline().ifPresent(timeline -> builder.timeline(timelineMapper.to(timeline)));

        partAdmissionResponse.getEvidence().ifPresent(evidence -> builder.evidence(evidenceMapper.to(evidence)));

        partAdmissionResponse.getStatementOfTruth()
            .ifPresent(statementOfTruth -> builder.statementOfTruth(statementOfTruthMapper.to(statementOfTruth)));

        return builder.build();
    }

    @Override
    public PartAdmissionResponse from(CCDPartAdmissionResponse ccdPartAdmissionResponse) {
        CCDYesNoOption ccdFreeMediation = ccdPartAdmissionResponse.getFreeMediationOption();
        CCDYesNoOption moreTimeNeeded = ccdPartAdmissionResponse.getMoreTimeNeededOption();
        CCDPaymentDeclaration paymentDeclaration = ccdPartAdmissionResponse.getPaymentDeclaration();
        CCDPaymentIntention paymentIntention = ccdPartAdmissionResponse.getPaymentIntention();

        PartAdmissionResponse.PartAdmissionResponseBuilder builder = PartAdmissionResponse.builder()
            .freeMediation(YesNoOption.valueOf(Optional.ofNullable(ccdFreeMediation).orElse(CCDYesNoOption.NO).name()))
            .moreTimeNeeded(YesNoOption.valueOf(Optional.ofNullable(moreTimeNeeded).orElse(CCDYesNoOption.NO).name()))
            .defendant(partyMapper.from(ccdPartAdmissionResponse.getDefendant()))
            .statementOfTruth(statementOfTruthMapper.from(ccdPartAdmissionResponse.getStatementOfTruth()))
            .amount(ccdPartAdmissionResponse.getAmount())
            .defence(ccdPartAdmissionResponse.getDefence())
            .timeline(timelineMapper.from(ccdPartAdmissionResponse.getTimeline()))
            .evidence(evidenceMapper.from(ccdPartAdmissionResponse.getEvidence()))
            .statementOfMeans(statementOfMeansMapper.from(ccdPartAdmissionResponse.getStatementOfMeans()));

        if (paymentDeclaration != null) {
            builder.paymentDeclaration(paymentDeclarationMapper.from(paymentDeclaration));
        }

        if (paymentIntention != null) {
            builder.paymentIntention(paymentIntentionMapper.from(paymentIntention));
        }

        return builder.build();
    }
}
