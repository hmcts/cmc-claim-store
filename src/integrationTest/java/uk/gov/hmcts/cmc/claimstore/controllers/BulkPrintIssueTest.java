package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false",
        "core_case_data.api.url=false"
    }
)
public class BulkPrintIssueTest extends BaseSaveTest {

    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;

    @Captor
    private ArgumentCaptor<DocumentReadyToPrintEvent> bulkPrintIssueEventArgument;
    @Captor
    private ArgumentCaptor<DocumentGeneratedEvent> documentGeneratedEventArgument;

    @Test
    public void shouldIssueBulkPrintEventIfEverythingIsFine() throws Exception {
        makeRequest(SampleClaimData.submittedByClaimant())
            .andExpect(status().isOk())
            .andReturn();

        verify(bulkPrintService).print(bulkPrintIssueEventArgument.capture());
        verify(claimIssuedStaffNotificationService).notifyStaffOfClaimIssue(documentGeneratedEventArgument.capture());
    }

}
