package uk.gov.hmcts.cmc.ccd.mapper;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@Component
public class CaseMapper implements Mapper<CCDCase, Claim> {

    private final ClaimMapper claimMapper;

    public CaseMapper(final ClaimMapper claimMapper) {
        this.claimMapper = claimMapper;
    }

    @Override
    public CCDCase to(Claim claim) {

        return CCDCase.builder()
            .externalId(claim.getExternalId())
            .referenceNumber(claim.getReferenceNumber())
            .submitterId(claim.getSubmitterId())
            .submitterEmail(claim.getSubmitterEmail())
            .issuedOn(claim.getIssuedOn().format(ISO_DATE))
            .submittedOn(claim.getCreatedAt().format(ISO_DATE_TIME))
            .claim(claimMapper.to(claim.getClaimData()))
            .build();
    }

    @Override
    public Claim from(CCDCase ccdCase) {
        throw new NotImplementedException("Not implemented yet!");
    }
}
