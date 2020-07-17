package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;
import java.time.LocalDateTime;

@Component
@SuppressWarnings({"LineLength"})
public class ClaimantResponseJsonMapper {



    @Autowired
    private final AddressJsonMapper address;

    public ClaimantResponseJsonMapper(AddressJsonMapper address) {
        this.address = address;
    }

    public JsonObject map(Claim claim) {
        ClaimantResponse response = claim.getClaimantResponse()
            .orElseThrow(() -> new IllegalArgumentException("Missing response"));
        String claimantEmail = claim.getSubmitterEmail();
        LocalDateTime claimantRespondedAt = claim.getClaimantRespondedAt().orElse(null);
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("responseSubmittedOn",DateFormatter.format(claimantRespondedAt))
            .add("claimantResponse", response.getType().toString())
            .add("claimant",mapClaimantDetails(claim))
            .build();

    }

    private JsonObject mapClaimantDetails(Claim claim) {
        return new NullAwareJsonObjectBuilder()
            .add("type", claim.getClaimData().getClaimant().getClass().getSimpleName())
            .add("name", claim.getClaimData().getClaimant().getName())
            .add("address", address.map(claim.getClaimData().getClaimant().getAddress())).build();

    }

}
