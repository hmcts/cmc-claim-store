package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.InterestAmount;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CalculateInterestAmountTest extends BaseSaveTest {

    @Test
    public void shouldCalculatedAmount() throws Exception {
        MvcResult result = makeRequest("2010-10-10", "2010-10-16", 30, 8000)
            .andExpect(status().isOk())
            .andReturn();

        InterestAmount obj = deserializeObjectFrom(result, InterestAmount.class);
        assertThat(obj.getAmount()).isEqualTo(BigDecimal.valueOf(39.45));
    }

    @Test
    public void shouldReturnBadRequestWhenDateFromIsEmpty() throws Exception {
        makeRequest("", "2010-10-10", 30, 8000)
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenDateToIsEmpty() throws Exception {
        makeRequest("2010-10-10", "", 30, 8000)
            .andExpect(status().isBadRequest());
    }

    @Test
    @Ignore // Enable back after fixing the dates problem correctly
    public void shouldReturnBadRequestWhenDateToIsBeforeDateFrom() throws Exception {
        makeRequest("2010-10-10", "2010-01-01", 30, 8000)
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenRateIsNegative() throws Exception {
        makeRequest("2010-10-10", "2010-01-01", -1, 8000)
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenAmountIsNegative() throws Exception {
        makeRequest("2010-10-10", "2010-01-01", 30, -1)
            .andExpect(status().isBadRequest());
    }

    private ResultActions makeRequest(String fromDate, String toDate, double rate, double amount) throws Exception {
        return webClient
            .perform(
                get(
                    String.format(
                        "/interest/calculate?from_date=%s&to_date=%s&rate=%s&amount=%s",
                        fromDate,
                        toDate,
                        String.valueOf(rate),
                        String.valueOf(amount)
                    )
                ).header(HttpHeaders.CONTENT_TYPE, "application/json")
            );
    }
}
