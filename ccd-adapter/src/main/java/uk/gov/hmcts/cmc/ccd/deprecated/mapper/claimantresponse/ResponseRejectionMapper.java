package uk.gov.hmcts.cmc.ccd.deprecated.mapper.claimantresponse;

import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

//@Component
public class ResponseRejectionMapper implements Mapper<CCDResponseRejection, ResponseRejection> {
    @Override
    public CCDResponseRejection to(ResponseRejection responseRejection) {
        Boolean mediation = responseRejection.getFreeMediation().orElse(CCDYesNoOption.NO.toBoolean());

        CCDResponseRejection.CCDResponseRejectionBuilder rejection = CCDResponseRejection.builder()
            .freeMediationOption(CCDYesNoOption.valueOf(mediation));

        responseRejection.getAmountPaid().ifPresent(rejection::amountPaid);
        responseRejection.getReason().ifPresent(rejection::reason);
        return rejection.build();
    }

    @Override
    public ResponseRejection from(CCDResponseRejection ccdResponseRejection) {
        ResponseRejection.ResponseRejectionBuilder builder = ResponseRejection.builder()
            .amountPaid(ccdResponseRejection.getAmountPaid())
            .reason(ccdResponseRejection.getReason());

        if (ccdResponseRejection.getFreeMediationOption() != null) {
            builder.freeMediation(ccdResponseRejection.getFreeMediationOption().toBoolean());
        }

        return builder.build();
    }
}
