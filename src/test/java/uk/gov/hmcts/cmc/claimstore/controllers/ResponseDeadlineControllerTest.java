package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResponseDeadlineControllerTest {
    private static final LocalDate SAMPLE_DATE = LocalDate.now().plusDays(100L);

    @InjectMocks
    private ResponseDeadlineController controller;

    @Mock
    private ResponseDeadlineCalculator calculator;

    @Test
    public void shouldPassThroughToCalculator() {
        // given
        when(calculator.calculatePostponedResponseDeadline(any(LocalDate.class)))
            .thenReturn(SAMPLE_DATE);

        // when
        LocalDate response = controller.postponedDeadline(LocalDate.now());

        // then
        assertThat(response).isEqualTo(SAMPLE_DATE);
    }
}
