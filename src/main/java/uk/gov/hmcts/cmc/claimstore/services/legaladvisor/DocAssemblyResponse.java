package uk.gov.hmcts.cmc.claimstore.services.legaladvisor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@EqualsAndHashCode
public class DocAssemblyResponse {

    private String renditionOutputLocation;
}
