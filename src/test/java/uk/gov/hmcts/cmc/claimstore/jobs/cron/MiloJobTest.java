package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.cmc.claimstore.services.MediationReportService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@ExtendWith(MockitoExtension.class)
class MiloJobTest {
    @Mock
    private MediationReportService mediationReportService;

    private MiloJob miloJob;

    @BeforeEach
    void setUp() {
        miloJob = new MiloJob();
        miloJob.setMediationReportService(mediationReportService);
    }

    @Test
    void executeShouldCallMediationReportService() throws Exception {
        miloJob.execute(null);

        verify(mediationReportService, once()).automatedMediationReport();
    }

    @Test
    void executeShouldThrowJobExecutionExceptionWhenExceptionThrown() throws Exception {
        doThrow(new Exception()).when(mediationReportService).automatedMediationReport();

        assertThrows(JobExecutionException.class, () -> miloJob.execute(null));
    }
}
