package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

@Component
public class RequestForJudgementJsonMapper {

    private final CountyCourtJudgementMapper countyCourtJudgementMapper;

    public RequestForJudgementJsonMapper(CountyCourtJudgementMapper countyCourtJudgementMapper) {
        this.countyCourtJudgementMapper = countyCourtJudgementMapper;
    }

    public JsonObject map(Claim claim) {
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("issueDate", DateFormatter.format(claim.getIssuedOn()))
            .add("courtFee", claim.getClaimData().getFeesPaidInPound())
            .add("amountWithInterest", claim.getTotalAmountTillToday().orElse(null))
            .add("countyCourtJudgement", countyCourtJudgementMapper.mapCCJ(claim.getCountyCourtJudgment()))
            .add("claimantEmail", claim.getSubmitterEmail())
            .add("defendantEmail", claim.getDefendantEmail())
            .build();
    }
}

