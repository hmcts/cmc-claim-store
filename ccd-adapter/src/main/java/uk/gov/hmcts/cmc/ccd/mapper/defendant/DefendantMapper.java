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

        CCDParty.CCDPartyBuilder partyDetail = CCDParty.builder();
        partyDetail.emailAddress(claim.getDefendantEmail());
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        builder.responseDeadline(claim.getResponseDeadline());
        builder.letterHolderId(claim.getLetterHolderId());
        builder.defendantId(claim.getDefendantId());

        builder.responseMoreTimeNeededOption(CCDYesNoOption.valueOf(claim.isMoreTimeRequested()));
        builder.directionsQuestionnaireDeadline(claim.getDirectionsQuestionnaireDeadline());
        builder.countyCourtJudgmentRequest(countyCourtJudgmentMapper.to(claim));

        claim.getSettlement().ifPresent(settlement ->
            builder.settlementPartyStatements(
                settlementMapper.toCCDPartyStatements(settlement)
            )
        );
        builder.settlementReachedAt(claim.getSettlementReachedAt());

        claim.getResponse().ifPresent(toResponse(claim, builder));
        theirDetailsMapper.to(builder, theirDetails);

        builder.claimantResponse(claimantResponseMapper.to(claim));
        claim.getMoneyReceivedOn().ifPresent(builder::paidInFullDate);

        reDeterminationMapper.to(builder, claim);

        return CCDCollectionElement.<CCDRespondent>builder()
            .value(builder.build())
            .id(theirDetails.getId())
            .build();
    }

    public TheirDetails from(Claim.ClaimBuilder builder, CCDCollectionElement<CCDRespondent> respondentElement) {

        CCDRespondent ccdRespondent = respondentElement.getValue();
        CCDParty partyDetail = ccdRespondent.getPartyDetail();

        builder
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
        responseMapper.from(builder, ccdRespondent);

        claimantResponseMapper.from(ccdRespondent.getClaimantResponse(), builder);

        reDeterminationMapper.from(builder, ccdRespondent);

        builder.moneyReceivedOn(ccdRespondent.getPaidInFullDate());

        return theirDetailsMapper.from(respondentElement);
    }

    private Consumer<Response> toResponse(Claim claim, CCDRespondent.CCDRespondentBuilder builder) {
        return response -> {
            responseMapper.to(builder, response);
            builder.responseSubmittedOn(claim.getRespondedAt());
        };
    }
}
