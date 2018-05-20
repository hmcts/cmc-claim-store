package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.CountyCourtJudgmentMapper;
import uk.gov.hmcts.cmc.ccd.mapper.offers.SettlementMapper;
import uk.gov.hmcts.cmc.ccd.mapper.response.ResponseMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.fromNullableUTCtoLocalZone;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.fromUTC;

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

        claim.getResponse().ifPresent(response -> builder.response(responseMapper.to((FullDefenceResponse) response)));

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
            .build();
    }

    @Override
    public Claim from(CCDCase ccdCase) {

        CountyCourtJudgment countyCourtJudgment = null;
        if (ccdCase.getCountyCourtJudgment() != null) {
            countyCourtJudgment = countyCourtJudgmentMapper.from(ccdCase.getCountyCourtJudgment());
        }

        Response response = null;
        if (ccdCase.getResponse() != null) {
            response = responseMapper.from(ccdCase.getResponse());
        }

        Settlement settlement = null;
        if (ccdCase.getSettlement() != null) {
            settlement = settlementMapper.from(ccdCase.getSettlement());
        }

        return new Claim(
            ccdCase.getId(),
            ccdCase.getSubmitterId(),
            ccdCase.getLetterHolderId(),
            ccdCase.getDefendantId(),
            ccdCase.getExternalId(),
            ccdCase.getReferenceNumber(),
            claimMapper.from(ccdCase.getClaimData()),
            fromUTC(LocalDateTime.parse(ccdCase.getSubmittedOn(), ISO_DATE_TIME)),
            LocalDate.parse(ccdCase.getIssuedOn(), ISO_DATE),
            ccdCase.getResponseDeadline(),
            ccdCase.getMoreTimeRequested() == YES,
            ccdCase.getSubmitterEmail(),
            fromNullableUTCtoLocalZone(ccdCase.getRespondedAt()),
            response,
            ccdCase.getDefendantEmail(),
            countyCourtJudgment,
            fromNullableUTCtoLocalZone(ccdCase.getCountyCourtJudgmentRequestedAt()),
            settlement,
            fromNullableUTCtoLocalZone(ccdCase.getSettlementReachedAt()),
            null
        );
    }
}
