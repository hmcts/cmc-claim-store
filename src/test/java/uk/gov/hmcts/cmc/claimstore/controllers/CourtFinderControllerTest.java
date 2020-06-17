package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.AreaOfLaw;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CourtFinderControllerTest {

    private static final String COURT_NAME = "A Court Name";

    private CourtFinderController courtFinderController;

    private final List<AreaOfLaw> moneyClaimAOEList = Collections
        .singletonList(AreaOfLaw.builder().name(CourtFinderController.MONEY_CLAIM_AOL).build());

    private final Court courtWithMoneyClaimAOE = Court.builder().name("Court A").areasOfLaw(moneyClaimAOEList).build();
    private final Court courtWithoutMoneyClaimAOE = Court.builder().name("Court B").areasOfLaw(emptyList()).build();

    @Mock
    private CourtFinderApi courtFinderApi;

    @Before
    public void setup() {
        courtFinderController = new CourtFinderController(courtFinderApi);
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
