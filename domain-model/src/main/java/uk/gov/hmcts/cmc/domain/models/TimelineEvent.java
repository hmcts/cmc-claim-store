package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode(callSuper = true)
@Getter
public class TimelineEvent extends CollectionId {

    @NotBlank
    @Size(max = 20)
    private final String date;

    @NotBlank
    @Size(max = 99000)
    private final String description;

    @Builder
    public TimelineEvent(String id, String eventDate, String description) {
        super(id);
        this.date = eventDate;
        this.description = description;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
