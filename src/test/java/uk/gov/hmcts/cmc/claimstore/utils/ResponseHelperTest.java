package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseHelperTest {

    @Test
    public void shouldReturnTrueWhenPartAdmission() {
        Claim claimWithPartAdmission = SampleClaim
            .builder().withResponse(SampleResponse.PartAdmission.builder().build()).build();
        assertThat(ResponseHelper.admissionResponse(claimWithPartAdmission)).isTrue();
    }

    @Test
    public void shouldReturnTrueWhenFullAdmission() {
        Claim claimWithPartAdmission = SampleClaim
            .builder().withResponse(SampleResponse.FullAdmission.builder().build()).build();
        assertThat(ResponseHelper.admissionResponse(claimWithPartAdmission)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenNotAdmissionsResponse() {
        Claim claimWithNoAdmissions = SampleClaim
            .builder().withResponse(SampleResponse.validDefaults()).build();
        assertThat(ResponseHelper.admissionResponse(claimWithNoAdmissions)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenNoResponsePresent() {
        Claim claimWithNoResponse = SampleClaim.builder().build();
        assertThat(ResponseHelper.admissionResponse(claimWithNoResponse)).isFalse();
    }

    @Test
    public void shouldReturnResponseType() {
        Claim claimWithNoAdmissions = SampleClaim
            .builder().withResponse(SampleResponse.validDefaults()).build();
        assertThat(ResponseHelper.getResponseType(claimWithNoAdmissions)).isNotNull();
    }

    @Test
    public void shouldNotReturnResponseType() {
        Claim claimWithNoResponse = SampleClaim.builder().build();
        assertThat(ResponseHelper.getResponseType(claimWithNoResponse)).isNull();
    }
}
