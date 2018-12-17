package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
public class CCDEmployment {
    private String jobTitle;
    private String employerName;
}
