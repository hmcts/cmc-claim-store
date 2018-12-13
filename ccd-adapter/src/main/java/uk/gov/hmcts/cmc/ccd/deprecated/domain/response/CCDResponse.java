package uk.gov.hmcts.cmc.ccd.deprecated.domain.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDResponse {

    private CCDResponseType responseType;
    private CCDFullDefenceResponse fullDefenceResponse;
    private CCDFullAdmissionResponse fullAdmissionResponse;
    private CCDPartAdmissionResponse partAdmissionResponse;
}
