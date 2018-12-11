package uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;

@Component
public class CourtOrderMapper implements Mapper<CCDCourtOrder, CourtOrder> {

    @Override
    public CCDCourtOrder to(CourtOrder courtOrder) {
        return CCDCourtOrder.builder()
            .claimNumber(courtOrder.getClaimNumber())
            .amountOwed(courtOrder.getAmountOwed())
            .monthlyInstalmentAmount(courtOrder.getMonthlyInstalmentAmount())
            .build();
    }

    @Override
    public CourtOrder from(CCDCourtOrder ccdCourtOrder) {
        return new CourtOrder(
            ccdCourtOrder.getClaimNumber(),
            ccdCourtOrder.getAmountOwed(),
            ccdCourtOrder.getMonthlyInstalmentAmount()
        );
    }
}
