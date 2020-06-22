package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.BulkPrintTransferService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BulkPrintTransferJobTest {

    private BulkPrintTransferJob bulkPrintTransferJob;

    @Mock
    private BulkPrintTransferService bulkPrintTransferService;

    @BeforeEach
    public void beforeEach() {
        bulkPrintTransferJob = new BulkPrintTransferJob(bulkPrintTransferService);
        doNothing().when(bulkPrintTransferService).bulkPrintTransfer();
    }

    @Test
    void executeShouldBulkPrintTransfer() throws JobExecutionException {
        bulkPrintTransferJob.execute(null);
        verify(bulkPrintTransferService, times(1)).bulkPrintTransfer();
    }
}
