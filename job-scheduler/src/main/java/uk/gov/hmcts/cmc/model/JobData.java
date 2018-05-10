package uk.gov.hmcts.cmc.model;

import lombok.Builder;
import lombok.Value;
import org.quartz.Job;

import java.time.ZonedDateTime;
import java.util.Map;

@Value
@Builder
public class JobData {
    private Class<? extends Job> jobClass;
    private String group;
    private String description;
    private Map<String, Object> data;
    private ZonedDateTime startDateTime;
}
