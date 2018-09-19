package uk.gov.hmcts.cmc.ccd.mapper.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

@Component
public class ResponseRejectionMapper implements Mapper<CCDResponseRejection, ResponseRejection> {
    @Override
    public CCDResponseRejection to(ResponseRejection responseRejection) {
        return CCDResponseRejection.builder()
            .amountPaid(responseRejection.getAmountPaid())
            .freeMediation(responseRejection.isFreeMediation())
            .reason(responseRejection.getReason())
            .build();
    }

    @Override
    public ResponseRejection from(CCDResponseRejection ccdResponseRejection) {
        return ResponseRejection.builder()
            .amountPaid(ccdResponseRejection.getAmountPaid())
            .freeMediation(ccdResponseRejection.isFreeMediation())
            .reason(ccdResponseRejection.getReason())
            .build();
    }
}
