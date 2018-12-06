package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import java.util.List;

@SuppressWarnings("squid:S1610")
public abstract class EvidenceMixIn {

    abstract List<EvidenceRow> getRows();
}
