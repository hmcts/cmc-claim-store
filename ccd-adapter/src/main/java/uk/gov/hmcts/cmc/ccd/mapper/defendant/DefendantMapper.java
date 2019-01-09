package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.mapper.TheirDetailsMapper;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.CountyCourtJudgmentMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@Component
public class DefendantMapper {

    private TheirDetailsMapper theirDetailsMapper;
    private ResponseMapper responseMapper;
    private CountyCourtJudgmentMapper ccjMapper;

    @Autowired
    public DefendantMapper(
        TheirDetailsMapper theirDetailsMapper,
        ResponseMapper responseMapper,
        CountyCourtJudgmentMapper countyCourtJudgmentMapper
    ) {
        this.theirDetailsMapper = theirDetailsMapper;
        this.responseMapper = responseMapper;
        this.ccjMapper = countyCourtJudgmentMapper;
    }

    public CCDDefendant to(TheirDetails theirDetails, Claim claim) {
        requireNonNull(theirDetails, "theirDetails must not be null");
        requireNonNull(claim, "claim must not be null");

        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        builder.responseDeadline(claim.getResponseDeadline());
        builder.letterHolderId(claim.getLetterHolderId());
        builder.defendantId(claim.getDefendantId());
        builder.partyEmail(claim.getDefendantEmail());
        builder.responseMoreTimeNeededOption(CCDYesNoOption.valueOf(claim.isMoreTimeRequested()));
        Optional.ofNullable(claim.getCountyCourtJudgment()).ifPresent(countyCourtJudgment ->
            builder.countyCourtJudgement(mapCCJFromClaim.apply(claim))
        );

        claim.getResponse().ifPresent(toResponse(claim, builder));
        theirDetailsMapper.to(builder, theirDetails);
        return builder.build();
    }

    public TheirDetails from(Claim.ClaimBuilder builder, CCDDefendant defendant) {
        builder
            .letterHolderId(defendant.getLetterHolderId())
            .responseDeadline(defendant.getResponseDeadline())
            .defendantEmail(defendant.getPartyEmail())
            .defendantId(defendant.getDefendantId());

        Optional.ofNullable(defendant.getCountyCourtJudgement()).ifPresent(ccj ->
            builder.countyCourtJudgment(ccjMapper.from(ccj))
        );

        Optional.ofNullable(defendant.getResponseMoreTimeNeededOption()).ifPresent(
            moreTimeNeeded -> builder.moreTimeRequested(moreTimeNeeded.toBoolean())
        );

        builder.respondedAt(defendant.getResponseSubmittedOn());
        responseMapper.from(builder, defendant);

        return theirDetailsMapper.from(defendant);
    }

    private Consumer<Response> toResponse(Claim claim, CCDDefendant.CCDDefendantBuilder builder) {
        return response -> {
            responseMapper.to(builder, response);
            builder.responseSubmittedOn(claim.getRespondedAt());
        };
    }

    private Function<Claim, CCDCountyCourtJudgment> mapCCJFromClaim = claim ->
        ccjMapper.to(claim.getCountyCourtJudgment()).toBuilder()
            .requestedDate(claim.getCountyCourtJudgmentRequestedAt()).build();
}
