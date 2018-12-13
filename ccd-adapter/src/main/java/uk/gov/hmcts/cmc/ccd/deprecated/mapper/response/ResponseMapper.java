package uk.gov.hmcts.cmc.ccd.deprecated.mapper.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDFullDefenceResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import static uk.gov.hmcts.cmc.ccd.domain.response.CCDResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.cmc.ccd.domain.response.CCDResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.cmc.ccd.domain.response.CCDResponseType.PART_ADMISSION;

//@Component
public class ResponseMapper implements Mapper<CCDResponse, Response> {

    private final FullDefenceResponseMapper fullDefenceResponseMapper;
    private final FullAdmissionResponseMapper fullAdmissionResponseMapper;
    private final PartAdmissionResponseMapper partAdmissionResponseMapper;

    @Autowired
    public ResponseMapper(
        FullDefenceResponseMapper fullDefenceResponseMapper,
        FullAdmissionResponseMapper fullAdmissionResponseMapper,
        PartAdmissionResponseMapper partAdmissionResponseMapper
    ) {
        this.fullDefenceResponseMapper = fullDefenceResponseMapper;
        this.fullAdmissionResponseMapper = fullAdmissionResponseMapper;
        this.partAdmissionResponseMapper = partAdmissionResponseMapper;
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
            case FULL_ADMISSION:
                FullAdmissionResponse fullAdmissionResponse = (FullAdmissionResponse) response;
                builder.responseType(FULL_ADMISSION)
                    .fullAdmissionResponse(fullAdmissionResponseMapper.to(fullAdmissionResponse));
                break;
            case PART_ADMISSION:
                PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) response;
                builder.responseType(PART_ADMISSION)
                    .partAdmissionResponse(partAdmissionResponseMapper.to(partAdmissionResponse));
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
            case FULL_ADMISSION:
                return fullAdmissionResponseMapper.from(ccdResponse.getFullAdmissionResponse());
            case PART_ADMISSION:
                return partAdmissionResponseMapper.from(ccdResponse.getPartAdmissionResponse());
            default:
                throw new MappingException("Invalid responseType " + ccdResponse.getResponseType());
        }
    }
}
