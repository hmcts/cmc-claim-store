package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDDefenceType;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDResponse;
import uk.gov.hmcts.cmc.ccd.mapper.DefendantEvidenceMapper;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.PartyMapper;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentDeclarationMapper;
import uk.gov.hmcts.cmc.ccd.mapper.StatementOfTruthMapper;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

@Component
public class ResponseMapper implements Mapper<CCDResponse, FullDefenceResponse> {

    private final StatementOfTruthMapper statementOfTruthMapper;
    private final PartyMapper partyMapper;
    private final PaymentDeclarationMapper paymentDeclarationMapper;
    private final DefendantTimelineMapper timelineMapper;
    private final DefendantEvidenceMapper evidenceMapper;

    @Autowired
    public ResponseMapper(
        StatementOfTruthMapper statementOfTruthMapper,
        PartyMapper partyMapper,
        PaymentDeclarationMapper paymentDeclarationMapper,
        DefendantTimelineMapper timelineMapper,
        DefendantEvidenceMapper evidenceMapper
    ) {

        this.statementOfTruthMapper = statementOfTruthMapper;
        this.partyMapper = partyMapper;
        this.paymentDeclarationMapper = paymentDeclarationMapper;
        this.timelineMapper = timelineMapper;
        this.evidenceMapper = evidenceMapper;
    }

    @Override
    public CCDResponse to(FullDefenceResponse response) {
        CCDResponse.CCDResponseBuilder builder = CCDResponse.builder();

        response.getFreeMediation()
            .ifPresent(freeMediation -> builder.freeMediationOption(CCDYesNoOption.valueOf(freeMediation.name())));

        if (response.getMoreTimeNeeded() == null) {
            builder.moreTimeNeededOption(CCDYesNoOption.valueOf(YesNoOption.NO.name()));
        } else {
            builder.moreTimeNeededOption(CCDYesNoOption.valueOf(response.getMoreTimeNeeded().name()));
        }

        builder.defendant(partyMapper.to(response.getDefendant()));

        response.getStatementOfTruth()
            .ifPresent(statementOfTruth -> builder.statementOfTruth(statementOfTruthMapper.to(statementOfTruth)));

        builder.responseType(CCDDefenceType.valueOf(response.getDefenceType().name()));
        response.getDefence().ifPresent(builder::defence);

        response.getPaymentDeclaration().ifPresent(paymentDeclaration ->
            builder.paymentDeclaration(paymentDeclarationMapper.to(paymentDeclaration)));

        response.getTimeline().ifPresent(timeline -> builder.timeline(timelineMapper.to(timeline)));

        response.getEvidence().ifPresent(evidence -> builder.evidence(evidenceMapper.to(evidence)));

        return builder.build();
    }

    @Override
    public FullDefenceResponse from(CCDResponse response) {
        YesNoOption freeMediation = null;

        if (response.getFreeMediationOption() != null) {
            freeMediation = YesNoOption.valueOf(response.getFreeMediationOption().name());
        }

        StatementOfTruth statementOfTruth = null;

        if (response.getStatementOfTruth() != null) {
            statementOfTruth = statementOfTruthMapper.from(response.getStatementOfTruth());
        }

        return new FullDefenceResponse(
            freeMediation,
            YesNoOption.valueOf(response.getMoreTimeNeededOption().name()),
            partyMapper.from(response.getDefendant()),
            statementOfTruth,
            DefenceType.valueOf(response.getResponseType().name()),
            response.getDefence(),
            paymentDeclarationMapper.from(response.getPaymentDeclaration()),
            timelineMapper.from(response.getTimeline()),
            evidenceMapper.from(response.getEvidence())
        );
    }
}
