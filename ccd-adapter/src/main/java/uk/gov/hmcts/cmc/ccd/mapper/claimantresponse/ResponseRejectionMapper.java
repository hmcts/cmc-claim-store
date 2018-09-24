package uk.gov.hmcts.cmc.ccd.mapper.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

import java.util.Optional;

@Component
public class ResponseRejectionMapper implements Mapper<CCDResponseRejection, ResponseRejection> {
    @Override
    public CCDResponseRejection to(ResponseRejection responseRejection) {
        Boolean mediation = Optional.ofNullable(responseRejection.isFreeMediation()).orElse(CCDYesNoOption.NO.toBoolean());

        return CCDResponseRejection.builder()
            .amountPaid(responseRejection.getAmountPaid())
            .freeMediationOption(CCDYesNoOption.valueOf(mediation))
            .reason(responseRejection.getReason())
            .build();
    }

    @Override
    public ResponseRejection from(CCDResponseRejection ccdResponseRejection) {
        ResponseRejection.ResponseRejectionBuilder builder = ResponseRejection.builder()
            .amountPaid(ccdResponseRejection.getAmountPaid())
            .reason(ccdResponseRejection.getReason());

        if(ccdResponseRejection.getFreeMediationOption() != null){
            builder.freeMediation(ccdResponseRejection.getFreeMediationOption().toBoolean());
        }

        return builder.build();
    }
}
