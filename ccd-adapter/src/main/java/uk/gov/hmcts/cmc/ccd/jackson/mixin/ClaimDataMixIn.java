package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.Timeline;
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
public interface ClaimDataMixIn {

    List<Party> getClaimants();

    List<TheirDetails> getDefendants();

    @JsonUnwrapped
    Interest getInterest();

    @JsonUnwrapped
    Payment getPayment();

    @JsonUnwrapped
    Optional<StatementOfTruth> getStatementOfTruth();

    @JsonUnwrapped
    Optional<HousingDisrepair> getHousingDisrepair();

    @JsonUnwrapped
    Optional<PersonalInjury> getPersonalInjury();

    @JsonIgnore
    UUID getExternalId();

//    @JsonUnwrapped
    Optional<Timeline> getTimeline();

    //    @JsonUnwrapped
    Optional<Evidence> getEvidence();
}
