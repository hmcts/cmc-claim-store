package uk.gov.hmcts.cmc.claimstore.services.courtfinder;

import com.google.common.collect.ImmutableList;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.CourtFinderController;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.AreaOfLaw;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Court;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.postcode.SearchCourtByPostcodeResponse;
import uk.gov.hmcts.cmc.claimstore.requests.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.test.utils.DataFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CourtFinderServiceTest {

    private static final String SEARCH_BY_SLUG_NEWCASTLE_RESPONSE = "factapi/courtfinder/search/response/slug/SEARCH_BY_SLUG_NEWCASTLE.json";
    private static final String SEARCH_BY_POSTCODE_NEWCASTLE_RESPONSE = "factapi/courtfinder/search/response/postcode/SEARCH_BY_POSTCODE_NEWCASTLE.json";
    private static final String COURT_NAME = "A Court Name";
    private final List<AreaOfLaw> moneyClaimAOEList = Collections
        .singletonList(AreaOfLaw.builder().name(CourtFinderService.MONEY_CLAIM_AOL).build());
    private final Court courtWithMoneyClaimAOE = Court.builder().name("Court A").areasOfLaw(moneyClaimAOEList).build();
    private final Court courtWithoutMoneyClaimAOE = Court.builder().name("Court B").areasOfLaw(emptyList()).build();
    private CourtFinderController courtFinderController;

    @Mock
    private CourtFinderApi courtFinderApi;

    @Mock
    private CourtFinderService courtFinderService;

    private FeignException createFeignException() {
        Map<String, Collection<String>> headers = new HashMap<>();
        Request request = Request.create(
            Request.HttpMethod.GET,
            "/test",
            headers,
            Request.Body.empty(),
            new RequestTemplate()
        );
        Response response = Response.builder()
            .status(500)
            .reason("failure")
            .request(request)
            .headers(headers)
            .body(new byte[0])
            .build();
        return FeignException.errorStatus("method", response);
    }

    @Before
    public void setup() {
        courtFinderController = new CourtFinderController(courtFinderService);
    }

    @Test
    public void shouldFilterCourtsWithoutMoneyClaimAOE() {
        when(courtFinderService.getCourtsByName(COURT_NAME))
            .thenReturn(ImmutableList.of(courtWithMoneyClaimAOE, courtWithoutMoneyClaimAOE));

        List<Court> output = courtFinderController.searchByName(COURT_NAME);

        assertThat(output.size()).isEqualTo(2);
        assertThat(output.get(0)).isEqualTo(courtWithMoneyClaimAOE);
    }

    @Test
    public void shouldGetNoCourtsByName() {
        List<Court> courtList = courtFinderService.getCourtsByName("Newcastle Civil & Family Courts and Tribunals Centre");
        assertThat(courtList.size()).isZero();
    }

    @Test
    public void shouldReturnEmptyListWhenPostcodeLookupFailsGracefully() {
        CourtFinderService service = new CourtFinderService(courtFinderApi);
        when(courtFinderApi.findMoneyClaimCourtByPostcode("NE1"))
            .thenThrow(createFeignException());

        List<Court> courts = service.getCourtDetailsListFromPostcode("NE1");

        assertThat(courts).isEmpty();
        verify(courtFinderApi, never()).getCourtDetailsFromNameSlug(anyString());
    }

    @Test
    public void shouldReturnSlugOnlyWhenCourtDetailsLookupFails() {
        CourtFinderService service = new CourtFinderService(courtFinderApi);
        SearchCourtByPostcodeResponse searchResponse =
            DataFactory.createSearchCourtByPostcodeResponseFromJson(SEARCH_BY_POSTCODE_NEWCASTLE_RESPONSE);
        String expectedSlug = searchResponse.getCourts().get(0).getSlug();

        when(courtFinderApi.findMoneyClaimCourtByPostcode("NE1"))
            .thenReturn(searchResponse);
        when(courtFinderApi.getCourtDetailsFromNameSlug(expectedSlug))
            .thenThrow(createFeignException());

        List<Court> courts = service.getCourtDetailsListFromPostcode("NE1");

        assertThat(courts).hasSize(1);
        Court fallbackCourt = courts.get(0);
        assertThat(fallbackCourt.getSlug()).isEqualTo(expectedSlug);
        assertThat(fallbackCourt.getName()).isNull();
    }

}
