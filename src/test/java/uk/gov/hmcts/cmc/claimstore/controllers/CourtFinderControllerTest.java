package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.AreaOfLaw;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Court;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.name.SearchCourtByNameResponse;
import uk.gov.hmcts.cmc.claimstore.requests.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.services.courtfinder.CourtFinderService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CourtFinderControllerTest {

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

    @Before
    public void setup() {
        courtFinderController = new CourtFinderController(courtFinderService);
    }

    @Test
    public void shouldFilterCourtsWithoutMoneyClaimAOE() {
        when(courtFinderApi.findMoneyClaimCourtByName(COURT_NAME))
            .thenReturn(ImmutableList.of(courtWithMoneyClaimAOE, courtWithoutMoneyClaimAOE));

        List<Court> output = courtFinderController.searchByName(COURT_NAME);

        assertThat(output.size()).isEqualTo(1);
        assertThat(output.get(0)).isEqualTo(courtWithMoneyClaimAOE);
    }

}
