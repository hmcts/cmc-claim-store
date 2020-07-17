package uk.gov.hmcts.cmc.rpa.mapper;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.config.ModuleConfiguration;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

@SpringBootTest
@ContextConfiguration(classes = ModuleConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimantResponseJsonMapperTest {

    private static final String REFERENCE_NUMBER = "000MC001";
    private static final String ACCEPTATION_INDIVIDUAL = "/claimant/claimant_acceptation_response_rpa_case.json";
    private static final String REJECTION_INDIVIDUAL = "/claimant/claimant_rejection_response_rpa_case.json";

    @Autowired
    private ClaimantResponseJsonMapper mapper;

    @Test
    public void ShouldMapClaimantResponseAcception() throws JSONException {
        Claim claim = SampleClaim.builder().withResponse(SampleResponse.validDefaults())
            .withReferenceNumber(REFERENCE_NUMBER)
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .withClaimantRespondedAt(LocalDateTime.of(2018, 4, 26, 1, 1))
            .build();

        String expected = new ResourceReader().read(ACCEPTATION_INDIVIDUAL).trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);

     }

    @Test
    public void ShouldMapClaimantResponseRejection() throws JSONException {
        Claim claim = SampleClaim.builder().withResponse(SampleResponse.validDefaults())
            .withReferenceNumber(REFERENCE_NUMBER)
            .withClaimantResponse(SampleClaimantResponse.validDefaultRejection())
            .withClaimantRespondedAt(LocalDateTime.of(2018, 4, 26, 1, 1))
            .build();

        String expected = new ResourceReader().read(REJECTION_INDIVIDUAL).trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);

    }
}

