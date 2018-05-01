package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false",
        "core_case_data.api.url=false",
        "feature_toggles.emailToStaff=false"
    }
)
public class SaveClaimWithStaffNotificationDisabledTest extends BaseSaveTest {
    @MockBean
    protected SendLetterApi sendLetterApi;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Test
    public void shouldNotSendStaffNotificationsForCitizenClaimIssuedEvent() throws Exception {
        makeRequest(SampleClaimData.submittedByClaimant())
            .andExpect(status().isOk())
            .andReturn();

        verify(emailService, never()).sendEmail(eq("sender@example.com"), emailDataArgument.capture());
    }
}
