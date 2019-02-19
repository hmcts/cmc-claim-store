package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseHelperTest {

    @Test
    public void shouldReturnTrueWhenPartAdmission() {
        Response partAdmissionResponse = SampleResponse.PartAdmission.builder().build();
        assertThat(ResponseHelper.admissionResponse(partAdmissionResponse)).isTrue();
    }

    @Test
    public void shouldReturnTrueWhenFullAdmission() {
        Response fullAdmissionResponse = SampleResponse.FullAdmission.builder().build();
        assertThat(ResponseHelper.admissionResponse(fullAdmissionResponse)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenNotAdmissionsResponse() {
        Response nonAdmissionResponse = SampleResponse.FullDefence.builder().build();
        assertThat(ResponseHelper.admissionResponse(nonAdmissionResponse)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenNullResponseIsSent() {
        assertThat(ResponseHelper.admissionResponse(null)).isFalse();
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
