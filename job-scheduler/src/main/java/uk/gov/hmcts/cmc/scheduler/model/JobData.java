package uk.gov.hmcts.cmc.scheduler.model;

import lombok.Builder;
import lombok.Value;
import org.quartz.Job;

import java.util.Map;

@Value
@Builder
public class JobData {
    private String id;
    private Class<? extends Job> jobClass;
    private String group;
    private String description;
    private Map<String, Object> data;
}
