package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.email.EmailService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RootTest extends BaseMockSpringTest {

    @MockBean
    protected EmailService emailService;

    @Test
    public void root() throws Exception {
        webClient.perform(get("/"))
            .andExpect(status().isOk());
    }

}
