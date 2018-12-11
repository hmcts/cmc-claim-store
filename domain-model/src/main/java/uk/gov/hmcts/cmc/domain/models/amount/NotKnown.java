package uk.gov.hmcts.cmc.domain.models.amount;

public class NotKnown extends Amount {
    public NotKnown() {
        super(AmountType.not_known.name());
    }
}
