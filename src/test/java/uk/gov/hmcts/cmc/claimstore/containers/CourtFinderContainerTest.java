package uk.gov.hmcts.cmc.claimstore.containers;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.LegacyCourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.CourtFinderResponse;
import uk.gov.hmcts.cmc.claimstore.test.utils.DataFactory;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class CourtFinderContainerTest {

    @Mock
    private CourtFinderApi courtFinderApi;

    @Mock
    private LegacyCourtFinderApi legacyCourtFinderApi;

    private static final String COURT_FINDER_RESPONSE_NEWCASTLE = "court-finder/response/NEWCASTLE_COURT_FINDER_RESPONSE.json";

    @Test
    public void shouldGetCourtsFromCourtFinderResponse() {
        CourtFinderResponse courtFinderResponse = DataFactory.createCourtFinderResponseFromJson(COURT_FINDER_RESPONSE_NEWCASTLE);

        List<Court> actualCourtsFromCourtFinderResponse = new CourtFinderContainer(courtFinderApi)
            .getCourtsFromCourtFinderResponse(courtFinderResponse);

        Assert.assertEquals(1, actualCourtsFromCourtFinderResponse.size());
    }

}
