package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.InterestAmount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CalculateInterestAmountTest extends BaseSaveTest {

    @Test
    public void shouldCalculatedAmount() throws Exception {

        MvcResult result = makeRequest("2010-10-10", "2010-10-16", 30, 8000)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, InterestAmount.class))
            .extracting(InterestAmount::getAmount)
            .isEqualTo(39.45);
    }

    protected ResultActions makeRequest(String fromDate, String toDate, double rate, double amount) throws Exception {
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
