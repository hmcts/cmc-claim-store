package uk.gov.hmcts.cmc.ccd.mapper.claimantresponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponseType;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

@Component
public class ClaimantResponseMapper implements Mapper<CCDClaimantResponse, ClaimantResponse> {

    private final ResponseAcceptationMapper responseAcceptationMapper;
    private final ResponseRejectionMapper responseRejectionMapper;

    @Autowired
    public ClaimantResponseMapper(ResponseAcceptationMapper responseAcceptationMapper,
                                  ResponseRejectionMapper responseRejectionMapper) {

        this.responseAcceptationMapper = responseAcceptationMapper;
        this.responseRejectionMapper = responseRejectionMapper;
    }

    @Override
    public CCDClaimantResponse to(ClaimantResponse claimantResponse) {
        CCDClaimantResponse.CCDClaimantResponseBuilder builder = CCDClaimantResponse.builder();
        switch (claimantResponse.getType()) {
            case ACCEPTATION:
                ResponseAcceptation responseAcceptation = (ResponseAcceptation) claimantResponse;
                builder.claimantResponseType(CCDClaimantResponseType.ACCEPTATION)
                    .responseAcceptation(responseAcceptationMapper.to(responseAcceptation));
                break;
            case REJECTION:
                ResponseRejection responseRejection = (ResponseRejection) claimantResponse;
                builder.claimantResponseType(CCDClaimantResponseType.REJECTION)
                    .responseRejection(responseRejectionMapper.to(responseRejection));
            default:
                throw new MappingException("Invalid claimant response type " + claimantResponse.getType());
        }
        return builder.build();
    }

    @Override
    public ClaimantResponse from(CCDClaimantResponse ccdClaimantResponse) {
        switch (ccdClaimantResponse.getClaimantResponseType()) {
            case ACCEPTATION:
                CCDResponseAcceptation responseAcceptation = ccdClaimantResponse.getResponseAcceptation();
                return responseAcceptationMapper.from(responseAcceptation);
            case REJECTION:
                CCDResponseRejection responseRejection = ccdClaimantResponse.getResponseRejection();
                return responseRejectionMapper.from(responseRejection);
            default:
                throw new MappingException("Invalid claimant response type " + ccdClaimantResponse.getClaimantResponseType());
        }

    }
}
