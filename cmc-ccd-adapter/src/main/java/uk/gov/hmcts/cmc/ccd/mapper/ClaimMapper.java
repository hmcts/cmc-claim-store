package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

@Component
public class ClaimMapper implements Mapper<CCDClaim, ClaimData> {

    @Override
    public CCDClaim to(ClaimData claimData) {
        return CCDClaim.builder()
            .build();
    }

    @Override
    public ClaimData from(CCDClaim ccdClaim) {
        return null;
    }
}
