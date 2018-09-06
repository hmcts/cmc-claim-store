package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDPartAdmissionResponse;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDPaymentDeclaration;
import uk.gov.hmcts.cmc.ccd.mapper.DefendantEvidenceMapper;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.PartyMapper;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentDeclarationMapper;
import uk.gov.hmcts.cmc.ccd.mapper.StatementOfTruthMapper;
import uk.gov.hmcts.cmc.ccd.mapper.statementofmeans.StatementOfMeansMapper;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

@Component
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
        return new PartAdmissionResponse(
            ccdFreeMediation != null ? YesNoOption.valueOf(ccdFreeMediation.name()) : null,
            moreTimeNeeded != null ? YesNoOption.valueOf(moreTimeNeeded.name()) : null,
            partyMapper.from(ccdPartAdmissionResponse.getDefendant()),
            statementOfTruthMapper.from(ccdPartAdmissionResponse.getStatementOfTruth()),
            ccdPartAdmissionResponse.getAmount(),
            paymentDeclaration != null ? paymentDeclarationMapper.from(paymentDeclaration) : null,
            paymentIntentionMapper.from(ccdPartAdmissionResponse.getPaymentIntention()),
            ccdPartAdmissionResponse.getDefence(),
            timelineMapper.from(ccdPartAdmissionResponse.getTimeline()),
            evidenceMapper.from(ccdPartAdmissionResponse.getEvidence()),
            statementOfMeansMapper.from(ccdPartAdmissionResponse.getStatementOfMeans())
        );
    }
}
