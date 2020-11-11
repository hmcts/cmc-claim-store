package uk.gov.hmcts.cmc.domain.models.evidence;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.evidence.EvidenceType.EXPERT_WITNESS;

@ExtendWith(MockitoExtension.class)
public class DefendantEvidenceTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings ={"Comment"})
    public void shouldPassValidationForValidDefendantEvidence(String input) {
        DefendantEvidence defendantEvidence = new DefendantEvidence(
            singletonList(EvidenceRow.builder().type(EXPERT_WITNESS).description("description").build()), input
        );

        Set<String> response = validate(defendantEvidence);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldPassValidationForMaxAllowedRows() {
        DefendantEvidence defendantEvidence = new DefendantEvidence(asList(new EvidenceRow[20]), "comments");

        Set<String> response = validate(defendantEvidence);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldFailValidationForRowsLimitExceeds() {
        DefendantEvidence defendantEvidence = new DefendantEvidence(asList(new EvidenceRow[1001]), "comments");

        Set<String> response = validate(defendantEvidence);

        assertThat(response)
            .hasSize(1)
            .contains("rows : size must be between 0 and 1000");
    }

    @Test
    public void shouldPFailValidationForTooLongComment() {
        DefendantEvidence defendantEvidence = new DefendantEvidence(
            singletonList(EvidenceRow.builder().type(EXPERT_WITNESS).description("description").build()),
            repeat("a", 99001)
        );

        Set<String> response = validate(defendantEvidence);

        assertThat(response)
            .hasSize(1)
            .contains("comment : size must be between 0 and 99000");
    }
}
