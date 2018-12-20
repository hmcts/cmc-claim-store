package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.mapper.TheirDetailsMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.util.function.Consumer;

@Component
public class DefendantMapper {

    private TheirDetailsMapper theirDetailsMapper;
    private ResponseMapper responseMapper;

    @Autowired
    public DefendantMapper(
        TheirDetailsMapper theirDetailsMapper,
        ResponseMapper responseMapper
    ) {
        this.theirDetailsMapper = theirDetailsMapper;
        this.responseMapper = responseMapper;
    }

    public CCDDefendant toLegal(TheirDetails theirDetails) {
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        theirDetailsMapper.to(builder, theirDetails);
        return builder.build();
    }

    public CCDDefendant toCitizen(TheirDetails theirDetails, Claim claim) {
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        builder.responseDeadline(claim.getResponseDeadline());
        builder.letterHolderId(claim.getLetterHolderId());
        claim.getResponse().ifPresent(mapResponse(claim, builder));
        theirDetailsMapper.to(builder, theirDetails);
        return builder.build();
    }

    public TheirDetails from(CCDDefendant defendant) {
        return theirDetailsMapper.from(defendant);
    }

    public TheirDetails from(Claim.ClaimBuilder builder, CCDDefendant defendant) {
        builder.letterHolderId(defendant.getLetterHolderId());
        builder.responseDeadline(defendant.getResponseDeadline());
        responseMapper.from(builder, defendant);

        return this.from(defendant);
    }

    private Consumer<Response> mapResponse(Claim claim, CCDDefendant.CCDDefendantBuilder builder) {
        return response -> {
            responseMapper.to(builder, response);
            builder.responseSubmittedDateTime(claim.getRespondedAt());
        };
    }
}
