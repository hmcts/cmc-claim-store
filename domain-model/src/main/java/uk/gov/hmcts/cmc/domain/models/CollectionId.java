package uk.gov.hmcts.cmc.domain.models;

import lombok.EqualsAndHashCode;

import java.util.Optional;
import java.util.UUID;

@EqualsAndHashCode
public abstract class CollectionId {
    private final String id;

    public CollectionId(String id) {
        this.id = Optional.ofNullable(id).orElse(UUID.randomUUID().toString());
    }

    public String getId() {
        return id;
    }

}

