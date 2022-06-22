package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.function.Predicate;

@Component
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {

    private static final String MIGRATION_ID = "YOUR_MIGRATION_ID_HERE";

    @Override
    public Predicate<CaseDetails> accepts() {
        /*
         Implement filter here that selects the cases to be migrated.
        */
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Map<String, Object> migrate(Map<String, Object> data) {
        // We are not using thi in our callback
        return Map.of("migrationId", MIGRATION_ID);
    }
}
