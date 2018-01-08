package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@Component
public class CaseMapper implements Mapper<CCDCase, Claim> {

    private final ClaimMapper claimMapper;

    public CaseMapper(ClaimMapper claimMapper) {
        this.claimMapper = claimMapper;
    }

    @Override
    public CCDCase to(Claim claim) {

        return CCDCase.builder()
            .id(claim.getId())
            .externalId(claim.getExternalId())
            .referenceNumber(claim.getReferenceNumber())
            .submitterId(claim.getSubmitterId())
            .submitterEmail(claim.getSubmitterEmail())
            .issuedOn(claim.getIssuedOn().format(ISO_DATE))
            .submittedOn(claim.getCreatedAt().format(ISO_DATE_TIME))
            .claimData(claimMapper.to(claim.getClaimData()))
            .build();
    }

    @Override
    public Claim from(CCDCase ccdCase) {
        return new Claim(
            ccdCase.getId(),
            ccdCase.getSubmitterId(),
            null,
            null,
            ccdCase.getExternalId(),
            ccdCase.getReferenceNumber(),
            claimMapper.from(ccdCase.getClaimData()),
            LocalDateTime.parse(ccdCase.getSubmittedOn(), ISO_DATE_TIME),
            LocalDate.parse(ccdCase.getIssuedOn(), ISO_DATE),
            null,
            false,
            ccdCase.getSubmitterEmail(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
}
