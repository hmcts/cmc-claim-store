package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDDefenceType;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDResponse;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.PartyMapper;
import uk.gov.hmcts.cmc.ccd.mapper.StatementOfTruthMapper;
import uk.gov.hmcts.cmc.domain.models.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.Response;

@Component
public class ResponseMapper implements Mapper<CCDResponse, FullDefenceResponse> {

    private final StatementOfTruthMapper statementOfTruthMapper;
    private final PartyMapper partyMapper;

    @Autowired
    public ResponseMapper(
        StatementOfTruthMapper statementOfTruthMapper,
        PartyMapper partyMapper) {

        this.statementOfTruthMapper = statementOfTruthMapper;
        this.partyMapper = partyMapper;
    }

    @Override
    public CCDResponse to(FullDefenceResponse response) {
        CCDResponse.CCDResponseBuilder builder = CCDResponse.builder();

        response.getFreeMediation()
            .ifPresent(freeMediation -> builder.freeMediation(freeMediation.name().toLowerCase()));

        builder.moreTimeNeeded(response.getMoreTimeNeeded().name());
        builder.defendant(partyMapper.to(response.getDefendant()));

        response.getStatementOfTruth()
            .ifPresent(statementOfTruth -> builder.statementOfTruth(statementOfTruthMapper.to(statementOfTruth)));

        builder.defenceType(CCDDefenceType.valueOf(response.getDefenceType().name()));
        builder.defence(response.getDefence());

        return builder.build();
    }

    @Override
    public FullDefenceResponse from(CCDResponse response) {

        return new FullDefenceResponse(
            Response.FreeMediationOption.valueOf(response.getFreeMediation().toUpperCase()),
            Response.MoreTimeNeededOption.valueOf(response.getMoreTimeNeeded().toUpperCase()),
            partyMapper.from(response.getDefendant()),
            statementOfTruthMapper.from(response.getStatementOfTruth()),
            FullDefenceResponse.DefenceType.valueOf(response.getDefenceType().name()),
            response.getDefence()
        );
    }
}
