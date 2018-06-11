package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class Employer {

    @NotBlank
    private final String jobTitle;

    @NotBlank
    private final String name;

    public Employer(String jobTitle, String name) {
        this.jobTitle = jobTitle;
        this.name = name;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Employer employer = (Employer) other;
        return Objects.equals(jobTitle, employer.jobTitle)
            && Objects.equals(name, employer.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobTitle, name);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
