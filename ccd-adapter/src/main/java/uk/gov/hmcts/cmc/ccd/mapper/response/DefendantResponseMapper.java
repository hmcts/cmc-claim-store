package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDDefendantResponse;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDFullDefenceResponse;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import static uk.gov.hmcts.cmc.ccd.domain.response.CCDResponseType.FULL_DEFENCE;

@Component
public class DefendantResponseMapper implements Mapper<CCDDefendantResponse, Response> {

    private final FullDefenceResponseMapper fullDefenceResponseMapper;

    @Autowired
    public DefendantResponseMapper(FullDefenceResponseMapper fullDefenceResponseMapper) {
        this.fullDefenceResponseMapper = fullDefenceResponseMapper;
    }

    @Override
    public CCDDefendantResponse to(Response response) {

        CCDDefendantResponse.CCDDefendantResponseBuilder builder = CCDDefendantResponse.builder();
        switch (response.getResponseType()) {
            case FULL_DEFENCE:
                FullDefenceResponse fullDefenceResponse = (FullDefenceResponse) response;
                builder.responseType(FULL_DEFENCE)
                    .fullDefenceResponse(fullDefenceResponseMapper.to(fullDefenceResponse));
                break;
            default:
                throw new MappingException("Invalid responseType " + response.getResponseType());
        }

        return builder.build();
    }

    @Override
    public Response from(CCDDefendantResponse ccdDefendantResponse) {
        switch (ccdDefendantResponse.getResponseType()) {
            case FULL_DEFENCE:
                CCDFullDefenceResponse fullDefenceResponse = ccdDefendantResponse.getFullDefenceResponse();
                return fullDefenceResponseMapper.from(fullDefenceResponse);
            default:
                throw new MappingException("Invalid responseType " + ccdDefendantResponse.getResponseType());
        }
    }
}
