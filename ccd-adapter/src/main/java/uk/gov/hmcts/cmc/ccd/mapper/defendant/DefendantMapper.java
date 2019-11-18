package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.mapper.TheirDetailsMapper;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.CountyCourtJudgmentMapper;
import uk.gov.hmcts.cmc.ccd.mapper.claimantresponse.ClaimantResponseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.offers.SettlementMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@Component
public class DefendantMapper {

    private final TheirDetailsMapper theirDetailsMapper;
    private final ResponseMapper responseMapper;
    private final ClaimantResponseMapper claimantResponseMapper;
    private final ReDeterminationMapper reDeterminationMapper;
    private final CountyCourtJudgmentMapper countyCourtJudgmentMapper;
    private final SettlementMapper settlementMapper;

    @Autowired
    public DefendantMapper(
        TheirDetailsMapper theirDetailsMapper,
        ResponseMapper responseMapper,
        CountyCourtJudgmentMapper countyCourtJudgmentMapper,
        ClaimantResponseMapper claimantResponseMapper,
        ReDeterminationMapper reDeterminationMapper,
        SettlementMapper settlementMapper
    ) {
        this.theirDetailsMapper = theirDetailsMapper;
        this.responseMapper = responseMapper;
        this.countyCourtJudgmentMapper = countyCourtJudgmentMapper;
        this.claimantResponseMapper = claimantResponseMapper;
        this.settlementMapper = settlementMapper;
        this.reDeterminationMapper = reDeterminationMapper;
    }

    public CCDCollectionElement<CCDRespondent> to(TheirDetails theirDetails, Claim claim) {
        requireNonNull(theirDetails, "theirDetails must not be null");
        requireNonNull(claim, "claim must not be null");

        CCDRespondent.CCDRespondentBuilder respondentBuilder = CCDRespondent.builder();
        respondentBuilder.servedDate(claim.getServiceDate());
        respondentBuilder.responseDeadline(claim.getResponseDeadline());
        respondentBuilder.letterHolderId(claim.getLetterHolderId());
        respondentBuilder.defendantId(claim.getDefendantId());

        CCDParty.CCDPartyBuilder partyDetail = CCDParty.builder();
        partyDetail.emailAddress(claim.getDefendantEmail());
        partyDetail.idamId(claim.getDefendantId());

        respondentBuilder.responseMoreTimeNeededOption(CCDYesNoOption.valueOf(claim.isMoreTimeRequested()));
        respondentBuilder.directionsQuestionnaireDeadline(claim.getDirectionsQuestionnaireDeadline());
        respondentBuilder.countyCourtJudgmentRequest(countyCourtJudgmentMapper.to(claim));
        claim.getFailedMediationReason().ifPresent(respondentBuilder::mediationFailedReason);
        claim.getMediationSettlementReachedAt().ifPresent(respondentBuilder::mediationSettlementReachedAt);

        claim.getSettlement().ifPresent(settlement ->
            respondentBuilder.settlementPartyStatements(
                settlementMapper.toCCDPartyStatements(settlement)
            )
        );
        respondentBuilder.settlementReachedAt(claim.getSettlementReachedAt());

        respondentBuilder.partyDetail(partyDetail.build());
        claim.getResponse().ifPresent(toResponse(claim, respondentBuilder, partyDetail));

        theirDetailsMapper.to(respondentBuilder, theirDetails);

        respondentBuilder.claimantResponse(claimantResponseMapper.to(claim));
        claim.getMoneyReceivedOn().ifPresent(respondentBuilder::paidInFullDate);

        reDeterminationMapper.to(respondentBuilder, claim);

        return CCDCollectionElement.<CCDRespondent>builder()
            .value(respondentBuilder.build())
            .id(theirDetails.getId())
            .build();
    }

    public TheirDetails from(Claim.ClaimBuilder builder, CCDCollectionElement<CCDRespondent> respondentElement) {

        CCDRespondent ccdRespondent = respondentElement.getValue();
        CCDParty partyDetail = ccdRespondent.getPartyDetail();

        builder
            .serviceDate(ccdRespondent.getServedDate())
            .letterHolderId(ccdRespondent.getLetterHolderId())
            .responseDeadline(ccdRespondent.getResponseDeadline())
            .defendantEmail(Optional.ofNullable(partyDetail)
                .map(CCDParty::getEmailAddress).orElse(null))
            .directionsQuestionnaireDeadline(ccdRespondent.getDirectionsQuestionnaireDeadline())
            .defendantId(ccdRespondent.getDefendantId());
        countyCourtJudgmentMapper.from(ccdRespondent.getCountyCourtJudgmentRequest(), builder);
        builder.settlement(settlementMapper.fromCCDDefendant(ccdRespondent));
        builder.settlementReachedAt(ccdRespondent.getSettlementReachedAt());

        Optional.ofNullable(ccdRespondent.getResponseMoreTimeNeededOption()).ifPresent(
            moreTimeNeeded -> builder.moreTimeRequested(moreTimeNeeded.toBoolean())
        );

        builder.respondedAt(ccdRespondent.getResponseSubmittedOn());
        Optional.ofNullable(ccdRespondent.getMediationFailedReason()).ifPresent(builder::failedMediationReason);
        Optional.ofNullable(ccdRespondent.getMediationSettlementReachedAt())
            .ifPresent(builder::mediationSettlementReachedAt);

        responseMapper.from(builder, respondentElement);

        claimantResponseMapper.from(ccdRespondent.getClaimantResponse(), builder);

        reDeterminationMapper.from(builder, ccdRespondent);

        builder.moneyReceivedOn(ccdRespondent.getPaidInFullDate());

        return theirDetailsMapper.from(respondentElement);
    }

    private Consumer<Response> toResponse(
        Claim claim,
        CCDRespondent.CCDRespondentBuilder builder,
        CCDParty.CCDPartyBuilder partyDetail
    ) {
        return response -> {
            responseMapper.to(builder, response, partyDetail);
            builder.responseSubmittedOn(claim.getRespondedAt());
        };
    }
}
