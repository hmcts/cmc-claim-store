package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

import java.math.BigDecimal;
import java.util.Optional;

@SuppressWarnings("squid:S1610")
public abstract class ClaimMixIn {

    @JsonUnwrapped
    abstract ClaimData getClaimData();

    @JsonIgnore
    abstract Optional<BigDecimal> getAmountWithInterest();

    @JsonIgnore
    abstract Optional<BigDecimal> getAmountWithInterestUntilIssueDate();

    @JsonIgnore
    abstract Optional<BigDecimal> getTotalAmountTillToday();

    @JsonIgnore
    abstract Optional<BigDecimal> getTotalAmountTillDateOfIssue();

    @JsonIgnore
    abstract Optional<BigDecimal> getTotalInterest();
}
