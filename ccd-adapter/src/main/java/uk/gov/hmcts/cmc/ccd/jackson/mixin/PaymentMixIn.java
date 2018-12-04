package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public abstract class PaymentMixIn {

    @JsonProperty("paymentAmount")
    abstract BigDecimal getAmount();

    @JsonProperty("paymentId")
    abstract String getId();

    @JsonProperty("paymentReference")
    abstract String getReference();

    @JsonProperty("paymentStatus")
    abstract String getStatus();

    @JsonProperty("paymentDateCreated")
    abstract String getDateCreated();
}
