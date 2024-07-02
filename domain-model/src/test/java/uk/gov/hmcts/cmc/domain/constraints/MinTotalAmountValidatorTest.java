package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.AmountRow;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MinTotalAmountValidatorTest {

    @Mock
    private MinTotalAmount annotation;
    @Mock
    private ConstraintValidatorContext context;

    private final MinTotalAmountValidator validator = new MinTotalAmountValidator();

    @BeforeEach
    public void beforeEachTest() {
        validator.initialize(with("0", false));
    }

    @Test
    public void initializeShouldThrowNumberFormatExceptionWhenNotConfiguredWithValidNumberString() {
        assertThrows(NumberFormatException.class, () -> {
            validator.initialize(with("not a valid number", false));
        });
    }

    @Test
    public void initializeShouldThrowNPEWhenConfiguredWithNullValue() {
        assertThrows(NullPointerException.class, () -> {
            validator.initialize(with(null, false));
        });
    }

    private MinTotalAmount with(String value, boolean inclusive) {
        when(annotation.value()).thenReturn(value);
        return annotation;
    }

    @Test
    public void shouldReturnTrueForNullValidationInput() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenMoreThan0IsRequiredAndGivenEmptyList() {
        assertThat(validator.isValid(emptyList(), context)).isFalse();
    }

    @Test
    public void shouldReturnTrueWhenMoreThan0IsRequiredAndGivenRowWithPositiveValue() {
        List<AmountRow> rows = singletonList(
            rowWithAmount("1")
        );
        assertThat(validator.isValid(rows, context)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenMoreThan0IsRequiredAndGivenRowWith0Value() {
        List<AmountRow> rows = singletonList(
            rowWithAmount("0")
        );
        assertThat(validator.isValid(rows, context)).isFalse();
    }

    private AmountRow rowWithAmount(String amountString) {
        if (amountString != null) {
            return AmountRow.builder().reason("Any reason").amount(new BigDecimal(amountString)).build();
        } else {
            return AmountRow.builder().reason("Any reason").amount(null).build();
        }
    }

    @Test
    public void shouldReturnFalseWhenMoreThan0IsRequiredAndGivenRowWithNullAmount() {
        List<AmountRow> rows = singletonList(
            rowWithAmount(null)
        );
        assertThat(validator.isValid(rows, context)).isFalse();
    }

    @Test
    public void shouldReturnTrueWhenMoreThan0IsRequiredAndGivenAtLeastOneRowWithMoreThan0() {
        List<AmountRow> rows = asList(
            rowWithAmount(null),
            rowWithAmount("0"),
            rowWithAmount(null),
            rowWithAmount("0.5")
        );
        assertThat(validator.isValid(rows, context)).isTrue();
    }

    @Test
    public void shouldReturnTrueWhenMoreThan10IsRequiredAndRowsSumUpToBeMore() {
        validator.initialize(with("10", false));
        List<AmountRow> rows = asList(
            rowWithAmount("3"),
            rowWithAmount("4"),
            rowWithAmount("4")
        );
        assertThat(validator.isValid(rows, context)).isTrue();
    }

    @Test
    public void shouldReturnTrueWhenAtLeastOneIsRequiredAndGivenRowWithOne() {
        when(annotation.inclusive()).thenReturn(true);

        validator.initialize(with("1", true));
        List<AmountRow> rows = singletonList(
            rowWithAmount("1")
        );
        assertThat(validator.isValid(rows, context)).isTrue();
    }

}
