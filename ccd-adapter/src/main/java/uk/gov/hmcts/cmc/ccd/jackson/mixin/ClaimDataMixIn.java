package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.Timeline;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.particulars.HousingDisrepair;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public abstract class ClaimDataMixIn {

    @JsonProperty("claimants")
    abstract List<Party> getClaimants();

    @JsonProperty("defendants")
    abstract List<TheirDetails> getDefendants();

    @JsonUnwrapped
    abstract Interest getInterest();

    @JsonUnwrapped
    abstract Payment getPayment();

    @JsonUnwrapped
    abstract Optional<StatementOfTruth> getStatementOfTruth();

    @JsonUnwrapped
    abstract Optional<HousingDisrepair> getHousingDisrepair();

    @JsonUnwrapped
    abstract Optional<Timeline> getTimeline();

    @JsonUnwrapped
    abstract Optional<Evidence> getEvidence();
}
