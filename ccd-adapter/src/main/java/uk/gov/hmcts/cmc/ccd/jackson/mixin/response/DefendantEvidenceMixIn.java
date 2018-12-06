package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import java.util.List;
import java.util.Optional;

public abstract class DefendantEvidenceMixIn {

    @JsonProperty("responseEvidenceRows")
    abstract List<EvidenceRow> getRows();

    @JsonProperty("responseEvidenceComment")
    abstract Optional<String> getComment();
}
