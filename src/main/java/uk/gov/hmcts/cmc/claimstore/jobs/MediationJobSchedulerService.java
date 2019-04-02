package uk.gov.hmcts.cmc.claimstore.jobs;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.CronScheduleBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.MediationEmailJob;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
public class MediationJobSchedulerService {

    public void scheduleMediation() {

        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            // define the job and tie it to our HelloJob class
            JobDetail job = newJob(MediationEmailJob.class)
                .withIdentity("Mediation Job")
                .build();

            // Trigger the job to run now, and then repeat every 40 seconds
            Trigger trigger = newTrigger()
                .withIdentity("Mediation Trigger")
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule("0 6 * * *"))
                .build();

            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(job, trigger);

            scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
