package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.Timeline;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.particulars.HousingDisrepair;
import uk.gov.hmcts.cmc.domain.models.particulars.PersonalInjury;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("squid:S1610")
public abstract class ClaimDataMixIn {

    abstract List<Party> getClaimants();

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
    abstract Optional<PersonalInjury> getPersonalInjury();

    @JsonIgnore
    abstract UUID getExternalId();

    //    @JsonUnwrapped
    Amount amount;

    //    @JsonUnwrapped
    abstract Optional<Timeline> getTimeline();

    //    @JsonUnwrapped
    abstract Optional<Evidence> getEvidence();
}
