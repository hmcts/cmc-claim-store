package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import java.util.List;

public interface EvidenceMixIn {

    List<EvidenceRow> getRows();
}
