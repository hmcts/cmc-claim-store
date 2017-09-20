package uk.gov.hmcts.cmc.claimstore.controllers.advices;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.BaseTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.EXTERNAL_ID;

public class ResourceExceptionHandlerTest extends BaseTest {


    @Test
    public void shouldReturnNotImplementedForPatchOnAnEndpoint() throws Exception {
        webClient
            .perform(patch("/claims/" + EXTERNAL_ID))
            .andExpect(status().isNotImplemented())
            .andReturn();
    }
}
