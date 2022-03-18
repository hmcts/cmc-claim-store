package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.CourtDetails;
import uk.gov.hmcts.cmc.email.EmailService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(value = "/environment.properties", properties = {
    "courtfinder.api.url=http://court-finder-api"
})
public class CourtFinderTest extends BaseMockSpringTest {

    @MockBean
    protected EmailService emailService;

    @Test
    public void shouldFindCourtThatExists() throws Exception {
        String courtSlug = "sluggity-slug";
        String courtName = "Dudley County Court and Family Court";

        given(courtFinderApi.getCourtDetailsFromNameSlug(courtSlug))
            .willReturn(CourtDetails.builder().name(courtName).build());

        MvcResult result = doGet("/court-finder/court-details/{slug}", courtSlug)
            .andExpect(status().isOk())
            .andReturn();

        CourtDetails courtDetails = jsonMappingHelper.deserializeObjectFrom(result, CourtDetails.class);
        assertThat(courtDetails.getName()).isEqualTo(courtName);
    }
}
