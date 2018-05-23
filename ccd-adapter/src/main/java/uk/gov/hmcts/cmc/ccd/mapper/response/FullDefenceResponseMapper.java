package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDDefenceType;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDFullDefenceResponse;
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
public class FullDefenceResponseMapper implements Mapper<CCDFullDefenceResponse, FullDefenceResponse> {

    private final StatementOfTruthMapper statementOfTruthMapper;
    private final PartyMapper partyMapper;
    private final PaymentDeclarationMapper paymentDeclarationMapper;
    private final DefendantTimelineMapper timelineMapper;
    private final DefendantEvidenceMapper evidenceMapper;

    @Autowired
    public FullDefenceResponseMapper(
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
    public CCDFullDefenceResponse to(FullDefenceResponse fullDefenceResponse) {
        CCDFullDefenceResponse.CCDFullDefenceResponseBuilder builder = CCDFullDefenceResponse.builder();

        fullDefenceResponse.getFreeMediation()
            .ifPresent(freeMediation -> builder.freeMediationOption(CCDYesNoOption.valueOf(freeMediation.name())));

        if (fullDefenceResponse.getMoreTimeNeeded() == null) {
            builder.moreTimeNeededOption(CCDYesNoOption.valueOf(YesNoOption.NO.name()));
        } else {
            builder.moreTimeNeededOption(CCDYesNoOption.valueOf(fullDefenceResponse.getMoreTimeNeeded().name()));
        }

        builder.defendant(partyMapper.to(fullDefenceResponse.getDefendant()));

        fullDefenceResponse.getStatementOfTruth()
            .ifPresent(statementOfTruth -> builder.statementOfTruth(statementOfTruthMapper.to(statementOfTruth)));

        builder.defenceType(CCDDefenceType.valueOf(fullDefenceResponse.getDefenceType().name()));
        fullDefenceResponse.getDefence().ifPresent(builder::defence);

        fullDefenceResponse.getPaymentDeclaration().ifPresent(paymentDeclaration ->
            builder.paymentDeclaration(paymentDeclarationMapper.to(paymentDeclaration)));

        fullDefenceResponse.getTimeline().ifPresent(timeline -> builder.timeline(timelineMapper.to(timeline)));

        fullDefenceResponse.getEvidence().ifPresent(evidence -> builder.evidence(evidenceMapper.to(evidence)));

        return builder.build();
    }

    @Override
    public FullDefenceResponse from(CCDFullDefenceResponse response) {
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
            DefenceType.valueOf(response.getDefenceType().name()),
            response.getDefence(),
            paymentDeclarationMapper.from(response.getPaymentDeclaration()),
            timelineMapper.from(response.getTimeline()),
            evidenceMapper.from(response.getEvidence())
        );
    }
}
