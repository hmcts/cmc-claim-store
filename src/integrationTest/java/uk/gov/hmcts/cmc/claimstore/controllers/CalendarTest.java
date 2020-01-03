package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.domain.models.NextWorkingDay;
import uk.gov.hmcts.cmc.email.EmailService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.toDate;

public class CalendarTest extends BaseMockSpringTest {

    @MockBean
    protected EmailService emailService;

    @Test
    public void shouldReturnNextWorkingDay() throws Exception {
        MvcResult result = makeRequest("2019-06-26")
            .andExpect(status().isOk())
            .andReturn();

        NextWorkingDay obj = jsonMappingHelper.deserializeObjectFrom(result, NextWorkingDay.class);
        assertThat(obj.getDate()).isEqualTo(toDate("2019-06-26"));
    }

    @Test
    public void shouldReturnBadRequestWhenDateIsMalformed() throws Exception {
        makeRequest("2019-10-100000").andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenDateIsInvalid() throws Exception {
        makeRequest("2019-13-10").andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenDateIsEmpty() throws Exception {
        makeRequest("").andExpect(status().isBadRequest());
    }

    protected ResultActions makeRequest(String date) throws Exception {
        return webClient
            .perform(
                get(
                    String.format(
                        "/calendar/next-working-day?date=%s",
                        date
                    )
                )
            );
    }
}
