package uk.gov.hmcts.cmc.domain.evidence;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.evidence.EvidenceType.PHOTO;

public class EvidenceTest {

    @Test
    public void shouldPassValidationForValidEvidence() {
        Evidence evidence = new Evidence(asList(new EvidenceRow(PHOTO, "description")));

        Set<String> response = validate(evidence);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldPassValidationForMaxAllowedEvidences() {
        Evidence evidence = new Evidence(asList(new EvidenceRow[20]));

        Set<String> response = validate(evidence);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldFailValidationForRowsLimitExceeds() {
        Evidence evidence = new Evidence(asList(new EvidenceRow[1001]));

        Set<String> response = validate(evidence);

        assertThat(response)
            .hasSize(1)
            .contains("rows : size must be between 0 and 1000");
    }

}
