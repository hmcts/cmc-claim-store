package uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDOnTaxPayments;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDSelfEmployment;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OnTaxPayments;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;

@Component
public class SelfEmploymentMapper implements Mapper<CCDSelfEmployment, SelfEmployment> {

    @Override
    public CCDSelfEmployment to(SelfEmployment selfEmployment) {
        if (selfEmployment == null) {
            return null;
        }
        CCDSelfEmployment.CCDSelfEmploymentBuilder builder = CCDSelfEmployment.builder()
            .jobTitle(selfEmployment.getJobTitle())
            .annualTurnover(selfEmployment.getAnnualTurnover());

        selfEmployment.getOnTaxPayments().ifPresent(onTaxPayments -> builder.onTaxPayments(
            CCDOnTaxPayments.builder()
                .amountYouOwe(onTaxPayments.getAmountYouOwe())
                .reason(onTaxPayments.getReason())
                .build()
        ));

        return builder.build();
    }

    @Override
    public SelfEmployment from(CCDSelfEmployment ccdSelfEmployment) {
        if (ccdSelfEmployment == null) {
            return null;
        }
        SelfEmployment.SelfEmploymentBuilder builder = SelfEmployment.builder()
            .jobTitle(ccdSelfEmployment.getJobTitle())
            .annualTurnover(ccdSelfEmployment.getAnnualTurnover());

        CCDOnTaxPayments ccdOnTaxPayments = ccdSelfEmployment.getOnTaxPayments();
        if (ccdOnTaxPayments != null) {
            builder.onTaxPayments(OnTaxPayments.builder()
                .amountYouOwe(ccdOnTaxPayments.getAmountYouOwe())
                .reason(ccdOnTaxPayments.getReason())
                .build());
        }

        return builder.build();
    }
}
