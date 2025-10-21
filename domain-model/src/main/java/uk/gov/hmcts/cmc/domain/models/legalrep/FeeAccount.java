package uk.gov.hmcts.cmc.domain.models.legalrep;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class FeeAccount {
    private final String reference;

    public FeeAccount(String reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

    public Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }
}
