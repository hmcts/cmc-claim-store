package uk.gov.hmcts.cmc.domain.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import javax.validation.constraints.Size;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class FreeMediation {

    private final YesNoOption freeMediation;

    @Size(max = 30, message = "may not be longer than {max} characters")
    private final String mediationMobilePhone;

    public FreeMediation(
        YesNoOption freeMediation,
        String mediationMobilePhone
    ) {
        this.freeMediation = freeMediation;
        this.mediationMobilePhone = mediationMobilePhone;
    }

    public YesNoOption getFreeMediation() {
        return freeMediation;
    }

    public Optional<String> getMediationMobilePhone() {
        return Optional.ofNullable(mediationMobilePhone);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
