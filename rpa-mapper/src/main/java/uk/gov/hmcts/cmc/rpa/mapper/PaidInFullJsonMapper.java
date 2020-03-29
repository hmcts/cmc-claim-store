package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.helper.RPAMapperHelper;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.json.JsonObject;

@Component
public class PaidInFullJsonMapper {

    public JsonObject map(Claim claim) {
        final LocalDate claimantPaidOnDate = RPAMapperHelper.claimantPaidOnDate(claim);

        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("paidInFullSubmittedOn", DateFormatter.format(LocalDateTime.now()))
            .add("claimantPaidOn", DateFormatter.format(claimantPaidOnDate))
            .build();
    }

}
