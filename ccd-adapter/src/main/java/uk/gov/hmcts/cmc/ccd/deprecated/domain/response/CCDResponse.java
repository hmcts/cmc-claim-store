package uk.gov.hmcts.cmc.ccd.deprecated.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType;

@Value
@Builder
public class CCDResponse {

    private CCDResponseType responseType;
    private CCDFullDefenceResponse fullDefenceResponse;
    private CCDFullAdmissionResponse fullAdmissionResponse;
    private CCDPartAdmissionResponse partAdmissionResponse;
}
