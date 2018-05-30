package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDFullDefenceResponse;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDResponse;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import static uk.gov.hmcts.cmc.ccd.domain.response.CCDResponseType.FULL_DEFENCE;

@Component
public class ResponseMapper implements Mapper<CCDResponse, Response> {

    private final FullDefenceResponseMapper fullDefenceResponseMapper;

    @Autowired
    public ResponseMapper(FullDefenceResponseMapper fullDefenceResponseMapper) {
        this.fullDefenceResponseMapper = fullDefenceResponseMapper;
    }

    @Override
    public CCDResponse to(Response response) {

        CCDResponse.CCDResponseBuilder builder = CCDResponse.builder();
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
    public Response from(CCDResponse ccdResponse) {
        switch (ccdResponse.getResponseType()) {
            case FULL_DEFENCE:
                CCDFullDefenceResponse fullDefenceResponse = ccdResponse.getFullDefenceResponse();
                return fullDefenceResponseMapper.from(fullDefenceResponse);
            default:
                throw new MappingException("Invalid responseType " + ccdResponse.getResponseType());
        }
    }
}
