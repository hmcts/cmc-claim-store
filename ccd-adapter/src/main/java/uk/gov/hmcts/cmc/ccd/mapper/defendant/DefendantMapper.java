package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.mapper.TheirDetailsMapper;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.CountyCourtJudgmentMapper;
import uk.gov.hmcts.cmc.ccd.mapper.claimantresponse.ClaimantResponseMapper;
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

    @Autowired
    public DefendantMapper(
        TheirDetailsMapper theirDetailsMapper,
        ResponseMapper responseMapper,
        CountyCourtJudgmentMapper countyCourtJudgmentMapper,
        ClaimantResponseMapper claimantResponseMapper,
        ReDeterminationMapper reDeterminationMapper
    ) {
        this.theirDetailsMapper = theirDetailsMapper;
        this.responseMapper = responseMapper;
        this.countyCourtJudgmentMapper = countyCourtJudgmentMapper;
        this.claimantResponseMapper = claimantResponseMapper;
        this.reDeterminationMapper = reDeterminationMapper;
    }

    public CCDCollectionElement<CCDDefendant> to(TheirDetails theirDetails, Claim claim) {
        requireNonNull(theirDetails, "theirDetails must not be null");
        requireNonNull(claim, "claim must not be null");

        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        builder.responseDeadline(claim.getResponseDeadline());
        builder.letterHolderId(claim.getLetterHolderId());
        builder.defendantId(claim.getDefendantId());
        builder.partyEmail(claim.getDefendantEmail());
        builder.responseMoreTimeNeededOption(CCDYesNoOption.valueOf(claim.isMoreTimeRequested()));
        builder.directionsQuestionnaireDeadline(claim.getDirectionsQuestionnaireDeadline());
        builder.countyCourtJudgementRequest(countyCourtJudgmentMapper.to(claim));

        claim.getResponse().ifPresent(toResponse(claim, builder));
        theirDetailsMapper.to(builder, theirDetails);

        builder.claimantResponse(claimantResponseMapper.to(claim));
        claim.getMoneyReceivedOn().ifPresent(builder::paidInFullDate);

        reDeterminationMapper.to(builder, claim);

        return CCDCollectionElement.<CCDDefendant>builder()
            .value(builder.build())
            .id(theirDetails.getId())
            .build();
    }

    public TheirDetails from(Claim.ClaimBuilder builder, CCDCollectionElement<CCDDefendant> defendant) {

        CCDDefendant ccdDefendant = defendant.getValue();

        builder
            .letterHolderId(ccdDefendant.getLetterHolderId())
            .responseDeadline(ccdDefendant.getResponseDeadline())
            .defendantEmail(ccdDefendant.getPartyEmail())
            .directionsQuestionnaireDeadline(ccdDefendant.getDirectionsQuestionnaireDeadline())
            .defendantId(ccdDefendant.getDefendantId());

        countyCourtJudgmentMapper.from(ccdDefendant.getCountyCourtJudgementRequest(), builder);

        Optional.ofNullable(ccdDefendant.getResponseMoreTimeNeededOption()).ifPresent(
            moreTimeNeeded -> builder.moreTimeRequested(moreTimeNeeded.toBoolean())
        );

        builder.respondedAt(ccdDefendant.getResponseSubmittedOn());
        responseMapper.from(builder, ccdDefendant);

        claimantResponseMapper.from(ccdDefendant.getClaimantResponse(), builder);

        reDeterminationMapper.from(builder, defendant);

        builder.moneyReceivedOn(defendant.getPaidInFullDate());

        return theirDetailsMapper.from(defendant);
    }

    private Consumer<Response> toResponse(Claim claim, CCDDefendant.CCDDefendantBuilder builder) {
        return response -> {
            responseMapper.to(builder, response);
            builder.responseSubmittedOn(claim.getRespondedAt());
        };
    }
}
