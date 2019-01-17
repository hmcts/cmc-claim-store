package uk.gov.hmcts.cmc.domain.models;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
public class TimelineEvent {

    @NotBlank
    @Size(max = 20)
    private final String date;

    @NotBlank
    @Size(max = 99000)
    private final String description;

    public TimelineEvent(String eventDate, String description) {
        this.date = eventDate;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
