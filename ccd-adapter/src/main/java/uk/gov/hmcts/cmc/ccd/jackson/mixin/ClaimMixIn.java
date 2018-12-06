package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

import java.math.BigDecimal;
import java.util.Optional;

public interface ClaimMixIn {

    @JsonUnwrapped
    ClaimData getClaimData();

    @JsonIgnore
    Optional<BigDecimal> getAmountWithInterest();

    @JsonIgnore
    Optional<BigDecimal> getAmountWithInterestUntilIssueDate();

    @JsonIgnore
    Optional<BigDecimal> getTotalAmountTillToday();

    @JsonIgnore
    Optional<BigDecimal> getTotalAmountTillDateOfIssue();

    @JsonIgnore
    Optional<BigDecimal> getTotalInterest();
}
