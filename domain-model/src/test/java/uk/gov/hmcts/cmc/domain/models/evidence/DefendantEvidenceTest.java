package uk.gov.hmcts.cmc.domain.models.evidence;

import org.junit.Test;

import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.evidence.EvidenceType.EXPERT_WITNESS;

public class DefendantEvidenceTest {

    @Test
    public void shouldPassValidationForValidDefendantEvidence() {
        DefendantEvidence defendantEvidence = new DefendantEvidence(
            singletonList(EvidenceRow.builder().type(EXPERT_WITNESS).description("description").build()), "comments"
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
    public void shouldPassValidationForNullComment() {
        DefendantEvidence defendantEvidence = new DefendantEvidence(
            singletonList(EvidenceRow.builder().type(EXPERT_WITNESS).description("description").build()), null
        );

        Set<String> response = validate(defendantEvidence);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldPassValidationForEmptyComment() {
        DefendantEvidence defendantEvidence = new DefendantEvidence(
            singletonList(EvidenceRow.builder().type(EXPERT_WITNESS).description("description").build()), ""
        );

        Set<String> response = validate(defendantEvidence);

        assertThat(response)
            .hasSize(0);
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
