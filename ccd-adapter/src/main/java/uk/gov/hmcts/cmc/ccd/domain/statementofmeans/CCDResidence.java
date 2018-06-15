package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;

@Builder
@Value
public class CCDResidence {
    private Residence.ResidenceType type;
    private String otherDetail;
}
