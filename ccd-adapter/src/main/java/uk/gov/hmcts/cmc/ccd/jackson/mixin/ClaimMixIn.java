package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.Timeline;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.particulars.HousingDisrepair;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public abstract class ClaimMixIn {

//    @JsonUnwrapped
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
