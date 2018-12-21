package uk.gov.hmcts.cmc.ccd.mapper.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Optional;

@Component
public class ResponseRejectionMapper implements Mapper<CCDResponseRejection, ResponseRejection> {
    @Override
    public CCDResponseRejection to(ResponseRejection responseRejection) {
        YesNoOption mediation = responseRejection.getFreeMediation().orElse(YesNoOption.NO);

        CCDResponseRejection.CCDResponseRejectionBuilder rejection = CCDResponseRejection.builder()
            .freeMediationOption(mediation);

        responseRejection.getAmountPaid().ifPresent(rejection::amountPaid);
        responseRejection.getReason().ifPresent(rejection::reason);
        return rejection.build();
    }

    @Override
    public ResponseRejection from(CCDResponseRejection ccdResponseRejection) {
        ResponseRejection.ResponseRejectionBuilder builder = ResponseRejection.builder()
            .amountPaid(ccdResponseRejection.getAmountPaid())
            .reason(ccdResponseRejection.getReason());

        Optional.ofNullable(ccdResponseRejection.getFreeMediationOption()).ifPresent(builder::freeMediation);

        return builder.build();
    }
}
