package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleRepaymentPlan;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SaveCountyCourtJudgmentTest extends BaseTest {

    @Before
    public void setup() {
        final UserDetails userDetails
            = SampleUserDetails.builder().withUserId(1L).withMail("claimant@email.com").build();

        given(userService.getUserDetails(anyString())).willReturn(userDetails);
        given(pdfServiceClient.generateFromHtml(any(), any())).willReturn(new byte[]{0, 0, 0});
    }

    @Test
    public void shouldReturnClaimWithCountyCourtJudgment() throws Exception {
        Claim claim = claimStore.save(SampleClaimData.builder().build(), 1L, LocalDate.now().minus(10, ChronoUnit.DAYS));
        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder().build();

        MvcResult result = request(claim.getId(), ccj)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getCountyCourtJudgment, Claim::getCountyCourtJudgmentRequestedAt)
            .doesNotContainNull()
            .contains(ccj);
    }

    @Test
    public void shouldFailWhenInvalidCountyCourtModelProvided() throws Exception {
        long anyClaimId = 500L;
        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withRepaymentPlan(SampleRepaymentPlan.builder().build())
            .withPaymentOption(PaymentOption.IMMEDIATELY)
            .build();

        MvcResult result = request(anyClaimId, ccj)
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(extractErrors(result)).hasSize(1)
            .contains("countyCourtJudgment : Invalid county court judgment request");
    }

    private ResultActions request(final long claimId, final CountyCourtJudgment ccj) throws Exception {
        return webClient
            .perform(post("/claims/" + claimId + "/county-court-judgment")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(ccj))
            );
    }
}
