package uk.gov.hmcts.cmc.claimstore.events.ccd;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class CCDTestingResponseDeadlineEvent {
    private final String claimReferenceNumber;
    private final String authorisation;
    private final LocalDate newDeadline;

    public CCDTestingResponseDeadlineEvent(String claimReferenceNumber, String authorisation, LocalDate newDeadline) {

        this.claimReferenceNumber = claimReferenceNumber;
        this.authorisation = authorisation;
        this.newDeadline = newDeadline;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
