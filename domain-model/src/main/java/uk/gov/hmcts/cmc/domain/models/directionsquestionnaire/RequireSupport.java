package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
public class RequireSupport {

    private final SupportType languageInterpreter;
    private final SupportType signLanguageInterpreter;
    private final SupportType hearingLoop;
    private final SupportType disabledAccess;
    private final SupportType otherSupport;

    @Builder
    public RequireSupport(
        SupportType languageInterpreter,
        SupportType signLanguageInterpreter,
        SupportType hearingLoop,
        SupportType disabledAccess,
        SupportType otherSupport
    ) {
        this.languageInterpreter = languageInterpreter;
        this.signLanguageInterpreter = signLanguageInterpreter;
        this.hearingLoop = hearingLoop;
        this.disabledAccess = disabledAccess;
        this.otherSupport = otherSupport;
    }

    public Optional<SupportType> getLanguageInterpreter() {
        return Optional.ofNullable(languageInterpreter);
    }

    public Optional<SupportType> getSignLanguageInterpreter() {
        return Optional.ofNullable(signLanguageInterpreter);
    }

    public Optional<SupportType> getHearingLoop() {
        return Optional.ofNullable(hearingLoop);
    }

    public Optional<SupportType> getDisabledAccess() {
        return Optional.ofNullable(disabledAccess);
    }

    public Optional<SupportType> getOtherSupport() {
        return Optional.ofNullable(otherSupport);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
