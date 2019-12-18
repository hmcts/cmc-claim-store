package uk.gov.hmcts.cmc.claimstore.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.CourtDetails;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.List;

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
    public void shouldFindPostcodeThatExists() throws Exception {
        String postcode = "SW1H9AJ";
        String courtName = "Dudley County Court and Family Court";

        given(courtFinderApi.findMoneyClaimCourtByPostcode(postcode))
            .willReturn(ImmutableList.of(Court.builder().name(courtName).build()));

        MvcResult result = makeGetRequest("/court-finder/search-postcode/" + postcode)
            .andExpect(status().isOk())
            .andReturn();

        List<Court> courts = jsonMappingHelper.fromJson(
            result.getResponse().getContentAsString(),
            new TypeReference<List<Court>>(){});

        assertThat(courts.get(0).getName()).isEqualTo(courtName);
    }

    @Test
    public void shouldFindCourtThatExists() throws Exception {
        String courtSlug = "sluggity-slug";
        String courtName = "Dudley County Court and Family Court";

        given(courtFinderApi.getCourtDetailsFromNameSlug(courtSlug))
            .willReturn(CourtDetails.builder().name(courtName).build());

        MvcResult result = makeGetRequest("/court-finder/court-details/" + courtSlug)
            .andExpect(status().isOk())
            .andReturn();

        CourtDetails courtDetails = jsonMappingHelper.deserializeObjectFrom(result, CourtDetails.class);
        assertThat(courtDetails.getName()).isEqualTo(courtName);
    }
}
