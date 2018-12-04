package uk.gov.hmcts.cmc.ccd.deserialize;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.math.BigDecimal;
import java.util.List;

public abstract class ClaimDataMixIn {

    @JsonProperty("listOfClaimants")
    abstract List<Party> getClaimants();

    @JsonProperty("listOfDefendants")
    abstract List<TheirDetails> getDefendants();

    @JsonUnwrapped
    abstract Interest getInterest();

    @JsonUnwrapped
    abstract Payment getPayment();
}
