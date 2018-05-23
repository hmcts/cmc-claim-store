package uk.gov.hmcts.cmc.ccd.domain.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDDefendantResponse {

    private CCDResponseType responseType;
    private CCDFullDefenceResponse fullDefenceResponse;
}
