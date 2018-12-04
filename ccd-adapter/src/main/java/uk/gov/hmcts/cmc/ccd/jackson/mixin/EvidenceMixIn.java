package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import java.util.List;

public abstract class EvidenceMixIn extends PartyMixIn {

    abstract List<EvidenceRow> getRows();
}
