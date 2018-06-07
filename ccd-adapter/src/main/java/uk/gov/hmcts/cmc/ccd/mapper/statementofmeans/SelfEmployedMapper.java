package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDSelfEmployed;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployed;

@Component
public class SelfEmployedMapper implements Mapper<CCDSelfEmployed, SelfEmployed> {

    @Override
    public CCDSelfEmployed to(SelfEmployed selfEmployed) {
        return CCDSelfEmployed.builder()
            .jobTitle(selfEmployed.getJobTitle())
            .annualTurnover(selfEmployed.getAnnualTurnover())
            .behindOnTaxPayments(CCDYesNoOption.valueOf(selfEmployed.isBehindOnTaxPayments().name()))
            .reason(selfEmployed.getReason())
            .amountYouOwe(selfEmployed.getAmountYouOwe())
            .build();
    }

    @Override
    public SelfEmployed from(CCDSelfEmployed ccdSelfEmployed) {
        if (ccdSelfEmployed == null) {
            return null;
        }
        return new SelfEmployed(
            ccdSelfEmployed.getJobTitle(),
            ccdSelfEmployed.getAnnualTurnover(),
            YesNoOption.valueOf(ccdSelfEmployed.getBehindOnTaxPayments().name()),
            ccdSelfEmployed.getAmountYouOwe(),
            ccdSelfEmployed.getReason()
        );
    }
}
