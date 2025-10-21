package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.utils.MonetaryConversions;

import java.math.BigDecimal;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
public class MoneyMapper implements Mapper<String, BigDecimal> {

    @Override
    public String to(BigDecimal amountInPounds) {
        return nonNull(amountInPounds) ? String.valueOf(MonetaryConversions.poundsToPennies(amountInPounds)) : null;
    }

    @Override
    public BigDecimal from(String amountInPennies) {
        return isNotEmpty(amountInPennies)
            ? MonetaryConversions.penniesToPounds(new BigDecimal(amountInPennies))
            : null;
    }
}
