package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Court;
import uk.gov.hmcts.cmc.claimstore.services.courtfinder.CourtFinderService;
import uk.gov.hmcts.cmc.claimstore.test.utils.DataFactory;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourtFinderControllerTest {

    private static final String SEARCH_BY_NAME_NEWCASTLE_RESPONSE = "factapi/courtfinder/search/response/name/SEARCH_BY_NAME_NEWCASTLE.json";

    private CourtFinderController courtFinderController;

    @Mock
    private CourtFinderService courtFinderService;

    @BeforeEach
    public void setup() {
        courtFinderController = new CourtFinderController(courtFinderService);
    }

    @Test
    public void shouldFilterCourtsWithoutMoneyClaimAOE() {

        Court court = DataFactory.createCourtFromJson(SEARCH_BY_NAME_NEWCASTLE_RESPONSE);
        List<Court> courtList = new ArrayList<>();
        courtList.add(court);

        when(courtFinderService.getCourtsByName(any()))
            .thenReturn(courtList);

        List<Court> output = courtFinderController.searchByName("Newcastle");

        assertThat(output.size()).isEqualTo(1);
    }

}
