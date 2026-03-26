package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class EachNotNullConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private EachNotNullConstraintValidator validator;

    @BeforeEach
    public void beforeEachTest() {
        validator = new EachNotNullConstraintValidator();
    }

    @Test
    public void shouldBeValidWhenGivenNullCollection() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    public void shouldBeValidWhenGivenEmptyCollection() {
        assertThat(validator.isValid(emptyList(), context)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenGivenCollectionWithNullElements() {
        assertThat(validator.isValid(asList(null, null), context)).isFalse();
    }

    @Test
    public void shouldBeValidWhenGivenCollectionWithAnObjectInstance() {
        assertThat(validator.isValid(asList("Hello there", 5), context)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenGivenCollectionWithMixedNullAndNotNullInstances() {
        assertThat(validator.isValid(asList("Hello there", null, 5, null), context)).isFalse();
    }

}
