package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Optional;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
public class RequireSupport {

    @Size(max = 100)
    private final String languageInterpreter;
    @Size(max = 100)
    private final String signLanguageInterpreter;
    private final YesNoOption hearingLoop;
    private final YesNoOption disabledAccess;
    @Size(max = 99000)
    private final String otherSupport;

    @Builder
    public RequireSupport(
        String languageInterpreter,
        String signLanguageInterpreter,
        YesNoOption hearingLoop,
        YesNoOption disabledAccess,
        String otherSupport
    ) {
        this.languageInterpreter = languageInterpreter;
        this.signLanguageInterpreter = signLanguageInterpreter;
        this.hearingLoop = hearingLoop;
        this.disabledAccess = disabledAccess;
        this.otherSupport = otherSupport;
    }

    public Optional<String> getLanguageInterpreter() {
        return Optional.ofNullable(languageInterpreter);
    }

    public Optional<String> getSignLanguageInterpreter() {
        return Optional.ofNullable(signLanguageInterpreter);
    }

    public Optional<YesNoOption> getHearingLoop() {
        return Optional.ofNullable(hearingLoop);
    }

    public Optional<YesNoOption> getDisabledAccess() {
        return Optional.ofNullable(disabledAccess);
    }

    public Optional<String> getOtherSupport() {
        return Optional.ofNullable(otherSupport);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
