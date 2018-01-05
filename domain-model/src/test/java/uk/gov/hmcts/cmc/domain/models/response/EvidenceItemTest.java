package uk.gov.hmcts.cmc.domain.models.response;

import org.junit.Test;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.BeanValidator.validate;

public class EvidenceItemTest {

    @Test
    public void shouldBeSuccessfulValidationForEvidence() {
        //given
        EvidenceItem evidenceItem = new EvidenceItem(EvidenceType.CONTRACTS_AND_AGREEMENTS, "my evidence");
        //when
        Set<String> errors = validate(evidenceItem);
        //then
        assertThat(errors).isEmpty();
    }
}
