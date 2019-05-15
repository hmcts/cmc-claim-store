package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.ccd.mapper.MoneyMapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;

@Component
public class CourtOrderMapper {

    private final MoneyMapper moneyMapper;

    @Autowired
    public CourtOrderMapper(MoneyMapper moneyMapper) {
        this.moneyMapper = moneyMapper;
    }

    public CCDCollectionElement<CCDCourtOrder> to(CourtOrder courtOrder) {
        if (courtOrder == null) {
            return null;
        }

        return CCDCollectionElement.<CCDCourtOrder>builder().value(CCDCourtOrder.builder()
            .claimNumber(courtOrder.getClaimNumber())
            .amountOwed(moneyMapper.to(courtOrder.getAmountOwed()))
            .monthlyInstalmentAmount(moneyMapper.to(courtOrder.getMonthlyInstalmentAmount()))
            .build())
            .id(courtOrder.getId())
            .build();
    }

    public CourtOrder from(CCDCollectionElement<CCDCourtOrder> collectionElement) {
        CCDCourtOrder ccdCourtOrder = collectionElement.getValue();

        if (ccdCourtOrder == null) {
            return null;
        }

        return CourtOrder.builder()
            .id(collectionElement.getId())
            .claimNumber(ccdCourtOrder.getClaimNumber())
            .amountOwed(moneyMapper.from(ccdCourtOrder.getAmountOwed()))
            .monthlyInstalmentAmount(moneyMapper.from(ccdCourtOrder.getMonthlyInstalmentAmount()))
            .build();
    }
}
