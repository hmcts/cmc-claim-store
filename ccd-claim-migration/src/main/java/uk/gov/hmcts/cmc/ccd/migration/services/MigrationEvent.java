package uk.gov.hmcts.cmc.ccd.migration.services;

import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.concurrent.atomic.AtomicInteger;

public class MigrationEvent {
    private final AtomicInteger migratedClaims;
    private final AtomicInteger failedMigrations;
    private final AtomicInteger updatedClaims;
    private final Claim claim;
    private final User user;

    public MigrationEvent(
        AtomicInteger migratedClaims,
        AtomicInteger failedMigrations,
        AtomicInteger updatedClaims,
        Claim claim,
        User user
    ) {
        this.migratedClaims = migratedClaims;
        this.failedMigrations = failedMigrations;
        this.updatedClaims = updatedClaims;
        this.claim = claim;
        this.user = user;
    }

    public AtomicInteger getMigratedClaims() {
        return migratedClaims;
    }

    public AtomicInteger getFailedMigrations() {
        return failedMigrations;
    }

    public AtomicInteger getUpdatedClaims() {
        return updatedClaims;
    }

    public Claim getClaim() {
        return claim;
    }

    public User getUser() {
        return user;
    }
}
