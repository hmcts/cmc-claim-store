package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.CountyCourtJudgmentMapper;
import uk.gov.hmcts.cmc.ccd.mapper.offers.SettlementMapper;
import uk.gov.hmcts.cmc.ccd.mapper.response.ResponseMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.fromNullableUTCtoLocalZone;

@Component
public class CaseMapper implements Mapper<CCDCase, Claim> {

    private final ClaimMapper claimMapper;
    private final CountyCourtJudgmentMapper countyCourtJudgmentMapper;
    private final ResponseMapper responseMapper;
    private final SettlementMapper settlementMapper;

    public CaseMapper(ClaimMapper claimMapper,
                      CountyCourtJudgmentMapper countyCourtJudgmentMapper,
                      ResponseMapper responseMapper,
                      SettlementMapper settlementMapper) {
        this.claimMapper = claimMapper;
        this.countyCourtJudgmentMapper = countyCourtJudgmentMapper;
        this.responseMapper = responseMapper;
        this.settlementMapper = settlementMapper;
    }

    @Override
    public CCDCase to(Claim claim) {

        CCDCase.CCDCaseBuilder builder = CCDCase.builder();

        if (claim.getCountyCourtJudgment() != null) {
            builder.countyCourtJudgment(countyCourtJudgmentMapper.to(claim.getCountyCourtJudgment()));
        }

        if (claim.getCountyCourtJudgmentRequestedAt() != null) {
            builder.countyCourtJudgmentRequestedAt(claim.getCountyCourtJudgmentRequestedAt());
        }

        if (claim.getRespondedAt() != null) {
            builder.respondedAt(claim.getRespondedAt());
        }

        claim.getResponse()
            .ifPresent(response -> builder.response(responseMapper.to(response)));

        claim.getSettlement().ifPresent(settlement -> builder.settlement(settlementMapper.to(settlement)));

        if (claim.getSettlementReachedAt() != null) {
            builder.settlementReachedAt(claim.getSettlementReachedAt());
        }

        if (claim.getLetterHolderId() != null) {
            builder.letterHolderId(claim.getLetterHolderId());
        }

        if (claim.getDefendantId() != null) {
            builder.defendantId(claim.getDefendantId());
        }

        claim.getSealedClaimDocument().ifPresent(document -> builder
            .sealedClaimDocument(CCDDocument.builder()
                .documentUrl(document.toString())
                .build())
        );

        return builder
            .id(claim.getId())
            .externalId(claim.getExternalId())
            .referenceNumber(claim.getReferenceNumber())
            .submitterId(claim.getSubmitterId())
            .submitterEmail(claim.getSubmitterEmail())
            .issuedOn(claim.getIssuedOn().format(ISO_DATE))
            .submittedOn(claim.getCreatedAt().format(ISO_DATE_TIME))
            .responseDeadline(claim.getResponseDeadline())
            .moreTimeRequested(claim.isMoreTimeRequested() ? YES : NO)
            .claimData(claimMapper.to(claim.getClaimData()))
            .defendantEmail(claim.getDefendantEmail())
            .features(claim.getFeatures() != null ? String.join(",", claim.getFeatures()) : null)
            .build();
    }

    @Override
    public Claim from(CCDCase ccdCase) {

        Claim.ClaimBuilder builder = Claim.builder()
            .id(ccdCase.getId())
            .submitterId(ccdCase.getSubmitterId())
            .letterHolderId(ccdCase.getLetterHolderId())
            .defendantId(ccdCase.getDefendantId())
            .externalId(ccdCase.getExternalId())
            .referenceNumber(ccdCase.getReferenceNumber())
            .claimData(claimMapper.from(ccdCase.getClaimData()))
            .createdAt(LocalDateTime.parse(ccdCase.getSubmittedOn(), ISO_DATE_TIME))
            .issuedOn(LocalDate.parse(ccdCase.getIssuedOn(), ISO_DATE))
            .responseDeadline(ccdCase.getResponseDeadline())
            .moreTimeRequested(ccdCase.getMoreTimeRequested() == YES)
            .submitterEmail(ccdCase.getSubmitterEmail())
            .respondedAt(fromNullableUTCtoLocalZone(ccdCase.getRespondedAt()))
            .defendantEmail(ccdCase.getDefendantEmail())
            .countyCourtJudgmentRequestedAt(fromNullableUTCtoLocalZone(ccdCase.getCountyCourtJudgmentRequestedAt()))
            .settlementReachedAt(fromNullableUTCtoLocalZone(ccdCase.getSettlementReachedAt()));

        if (ccdCase.getCountyCourtJudgment() != null) {
            builder.countyCourtJudgment(countyCourtJudgmentMapper.from(ccdCase.getCountyCourtJudgment()));
        }

        if (ccdCase.getResponse() != null) {
            builder.response(responseMapper.from(ccdCase.getResponse()));
        }

        if (ccdCase.getSettlement() != null) {
            builder.settlement(settlementMapper.from(ccdCase.getSettlement()));
        }

        if (ccdCase.getFeatures() != null) {
            builder.features(Arrays.asList(ccdCase.getFeatures().split(",")));
        }

        if (ccdCase.getSealedClaimDocument() != null) {
            builder.sealedClaimDocument(URI.create(ccdCase.getSealedClaimDocument().getDocumentUrl()));
        }

        return builder.build();
    }
}
