package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.CountyCourtJudgmentMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@Component
public class CaseMapper implements Mapper<CCDCase, Claim> {

    private final ClaimMapper claimMapper;
    private final CountyCourtJudgmentMapper countyCourtJudgmentMapper;

    public CaseMapper(ClaimMapper claimMapper, CountyCourtJudgmentMapper countyCourtJudgmentMapper) {
        this.claimMapper = claimMapper;
        this.countyCourtJudgmentMapper = countyCourtJudgmentMapper;
    }

    @Override
    public CCDCase to(Claim claim) {

        CCDCase.CCDCaseBuilder builder = CCDCase.builder();

        if (claim.getCountyCourtJudgment() != null) {
            builder.countyCourtJudgment(countyCourtJudgmentMapper.to(claim.getCountyCourtJudgment()));
        }

        if (claim.getCountyCourtJudgmentRequestedAt() != null) {
            builder.countyCourtJudgmentRequestedAt(claim.getCountyCourtJudgmentRequestedAt().format(ISO_DATE_TIME));
        }

        return builder
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

        CountyCourtJudgment countyCourtJudgment = null;
        if (ccdCase.getCountyCourtJudgment() != null) {
            countyCourtJudgment = countyCourtJudgmentMapper.from(ccdCase.getCountyCourtJudgment());
        }

        LocalDateTime countyCourtJudgmentRequestedAt = null;
        if (ccdCase.getCountyCourtJudgmentRequestedAt() != null) {
            countyCourtJudgmentRequestedAt = LocalDateTime.parse(ccdCase.getCountyCourtJudgmentRequestedAt());
        }

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
            countyCourtJudgment,
            countyCourtJudgmentRequestedAt,
            null,
            null,
            null
        );
    }
}
