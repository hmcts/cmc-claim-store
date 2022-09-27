package uk.gov.hmcts.cmc.claimstore.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class DocAssemblyAdditionalFieldsConfiguration {

    private final String caseTypeId;
    private final String jurisdictionId;
    private final boolean secureDocStoreEnabled;

    public DocAssemblyAdditionalFieldsConfiguration(@Value("${ocmc.caseTypeId") String caseTypeId,
                                                    @Value("${ocmc.jurisdictionId}") String jurisdictionId,
                                                    @Value("${ocmc.secureDocStoreEnabled}") boolean secureDocStoreEnabled) {
        this.caseTypeId = caseTypeId;
        this.jurisdictionId = jurisdictionId;
        this.secureDocStoreEnabled = secureDocStoreEnabled;
    }
}
