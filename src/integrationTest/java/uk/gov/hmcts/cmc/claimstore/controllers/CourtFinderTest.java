package uk.gov.hmcts.cmc.claimstore.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Court;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.postcode.CourtDetails;
import uk.gov.hmcts.cmc.claimstore.test.utils.DataFactory;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
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
        String courtName = "Newcastle Civil & Family Courts and Tribunals Centre";

        given(courtFinderApi.getCourtDetailsFromNameSlug(anyString()))
            .willReturn(DataFactory.createSearchCourtBySlugResponseFromJson("factapi/courtfinder/search/response/slug/SEARCH_BY_SLUG_NEWCASTLE.json"));

        given(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
            .willReturn(DataFactory.createSearchCourtByPostcodeResponseFromJson("factapi/courtfinder/search/response/postcode/SEARCH_BY_POSTCODE_NEWCASTLE.json"));

        MvcResult result = doGet("/court-finder/search-postcode/{postcode}", postcode)
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
        String courtName = "Newcastle Civil & Family Courts and Tribunals Centre";

        given(courtFinderApi.getCourtDetailsFromNameSlug(courtSlug))
            .willReturn(DataFactory.createSearchCourtBySlugResponseFromJson("factapi/courtfinder/search/response/slug/SEARCH_BY_SLUG_NEWCASTLE.json"));

        MvcResult result = doGet("/court-finder/court-details/{slug}", courtSlug)
            .andExpect(status().isOk())
            .andReturn();

        CourtDetails courtDetails = jsonMappingHelper.deserializeObjectFrom(result, CourtDetails.class);
        assertThat(courtDetails.getName()).isEqualTo(courtName);
    }
}
