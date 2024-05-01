package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import java.time.LocalDateTime;
import jakarta.json.JsonObject;

@Component
public class MoreTimeRequestedJsonMapper {

    public JsonObject map(Claim claim, LocalDateTime moreTimeRequestOn) {
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("moreTimeRequestedOn", DateFormatter.format(moreTimeRequestOn))
            .add("responseDeadline", DateFormatter.format(claim.getResponseDeadline()))
            .build();
    }

}
