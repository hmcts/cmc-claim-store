package uk.gov.hmcts.cmc.ccd.migration.services;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MigratorProducer {
    private final ApplicationEventPublisher publisher;

    public MigratorProducer(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void createEvent(
        AtomicInteger migratedClaims,
        AtomicInteger failedMigrations,
        AtomicInteger updatedClaims,
        Claim claim,
        User user
    ) {

        publisher.publishEvent(new MigrationEvent(migratedClaims, failedMigrations, updatedClaims, claim, user));
    }
}
