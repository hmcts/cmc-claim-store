package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDDefenceType;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDResponse;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.PartyMapper;
import uk.gov.hmcts.cmc.ccd.mapper.StatementOfTruthMapper;
import uk.gov.hmcts.cmc.domain.models.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

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
            .ifPresent(freeMediation -> builder.freeMediationOption(CCDYesNoOption.valueOf(freeMediation.name())));

        builder.moreTimeNeededOption(CCDYesNoOption.valueOf(response.getMoreTimeNeeded().name()));
        builder.defendant(partyMapper.to(response.getDefendant()));

        response.getStatementOfTruth()
            .ifPresent(statementOfTruth -> builder.statementOfTruth(statementOfTruthMapper.to(statementOfTruth)));

        builder.responseType(CCDDefenceType.valueOf(response.getDefenceType().name()));
        builder.defence(response.getDefence());

        return builder.build();
    }

    @Override
    public FullDefenceResponse from(CCDResponse response) {
        Response.FreeMediationOption freeMediation = null;

        if (response.getFreeMediationOption() != null) {
            freeMediation = Response.FreeMediationOption.valueOf(response.getFreeMediationOption().name());
        }

        StatementOfTruth statementOfTruth = null;

        if (response.getStatementOfTruth() != null) {
            statementOfTruth = statementOfTruthMapper.from(response.getStatementOfTruth());
        }
        
        return new FullDefenceResponse(
            freeMediation,
            Response.MoreTimeNeededOption.valueOf(response.getMoreTimeNeededOption().name()),
            partyMapper.from(response.getDefendant()),
            statementOfTruth,
            FullDefenceResponse.DefenceType.valueOf(response.getResponseType().name()),
            response.getDefence()
        );
    }
}
