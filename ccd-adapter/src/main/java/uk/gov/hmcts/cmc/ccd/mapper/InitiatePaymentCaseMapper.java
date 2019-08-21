package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.ioc.InitiatePaymentRequest;

import java.util.UUID;

@Component
public class InitiatePaymentCaseMapper implements
    BuilderMapper<CCDCase, InitiatePaymentRequest, CCDCase.CCDCaseBuilder> {
    private AmountMapper amountMapper;
    private InterestMapper interestMapper;

    public InitiatePaymentCaseMapper(
        AmountMapper amountMapper,
        InterestMapper interestMapper
    ) {
        this.amountMapper = amountMapper;
        this.interestMapper = interestMapper;
    }

    @Override
    public void to(InitiatePaymentRequest request, CCDCase.CCDCaseBuilder builder) {
        interestMapper.to(request.getInterest(), builder);
        amountMapper.to(request.getAmount(), builder);

        builder
            .externalId(request.getExternalId().toString())
            .issuedOn(request.getIssuedOn());
    }

    @Override
    public InitiatePaymentRequest from(CCDCase ccdCase) {
        return InitiatePaymentRequest.builder()
            .externalId(UUID.fromString(ccdCase.getExternalId()))
            .amount(amountMapper.from(ccdCase))
            .issuedOn(ccdCase.getIssuedOn())
            .interest(interestMapper.from(ccdCase))
            .build();
    }
}
