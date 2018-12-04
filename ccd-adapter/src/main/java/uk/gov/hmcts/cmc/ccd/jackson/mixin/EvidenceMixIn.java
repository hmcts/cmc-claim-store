package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import java.util.List;

public abstract class EvidenceMixIn extends PartyMixIn {

    @JsonProperty("evidence")
    abstract List<EvidenceRow> getRows();
}
