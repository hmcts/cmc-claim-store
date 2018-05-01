package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class PostcodeConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private PostcodeConstraintValidator validator;

    @Before
    public void beforeEachTest() {
        validator = new PostcodeConstraintValidator();
    }

    @Test
    public void shouldBeValidForNullInput() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    public void shouldBeValidWhenGivenValidPostcode() {
        assertThat(validator.isValid("SW1H 9HE", context)).isTrue();
    }

    @Test
    public void shouldBeValidWhenGivenValidPostcodeWithoutSpace() {
        assertThat(validator.isValid("SW1H9HE", context)).isTrue();
    }

    @Test
    public void shouldBeValidWhenGivenValidPostcodeInLowerCase() {
        assertThat(validator.isValid("sw1h9he", context)).isTrue();
    }

    @Test
    public void shouldBeValidWhenGivenValidFormatOfANNAA() {
        assertThat(validator.isValid("M1 1AA", context)).isTrue();
    }

    @Test
    public void shouldBeValidWhenGivenValidFormatOfANNNAA() {
        assertThat(validator.isValid("M60 1NW", context)).isTrue();
    }

    @Test
    public void shouldBeValidWhenGivenValidFormatOfAANNAA() {
        assertThat(validator.isValid("CR2 6HX", context)).isTrue();
    }

    @Test
    public void shouldBeValidWhenGivenValidFormatOfAANNNAA() {
        assertThat(validator.isValid("DN55 1PT", context)).isTrue();
    }

    @Test
    public void shouldBeValidWhenGivenValidFormatOfANANAA() {
        assertThat(validator.isValid("W1P 1HQ", context)).isTrue();
    }

    @Test
    public void shouldBeValidWhenGivenValidFormatOfAANANAA() {
        assertThat(validator.isValid("EC1A 1BB", context)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenGivenEmptyPostcode() {
        assertThat(validator.isValid("", context)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenGivenSpaces() {
        assertThat(validator.isValid("   ", context)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenGivenPartialPostcode() {
        assertThat(validator.isValid("SW1H", context)).isFalse();
    }

    @Test
    public void shouldBeInvalidForGarbageString() {
        assertThat(validator.isValid("!@#$%^&*$$", context)).isFalse();
    }
}
