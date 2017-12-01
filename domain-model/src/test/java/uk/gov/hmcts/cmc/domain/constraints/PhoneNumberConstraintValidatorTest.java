package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PhoneNumberConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private PhoneNumberConstraintValidator validator;

    @Before
    public void beforeEachTest() {
        validator = new PhoneNumberConstraintValidator();
    }

    @Test
    public void shouldBeValidForNullInput() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    public void shouldNotBeValidForEmptyInput() {
        assertThat(validator.isValid("", context)).isFalse();
    }

    @Test
    public void shouldBeValidWhenGivenValid10DigitsLandLineNumber() {
        assertThat(validator.isValid("+44 (0203) 334 3555", context)).isTrue();
    }

    @Test
    public void shouldBeValidWhenGivenValid9DigitsLandLineNumber() {
        assertThat(validator.isValid("+44 (0203) 334 355", context)).isTrue();
    }

    @Test
    public void shouldBeValidWhenGivenValid7DigitsLandLineNumber() {
        assertThat(validator.isValid("+44 (0203) 3555", context)).isTrue();
    }

    @Test
    public void shouldBeInvalidForInvalidLandLineNumber() {
        assertThat(validator.isValid("+44 (0203) 35559", context)).isFalse();
    }

    @Test
    public void shouldBeValidForValidMobileNumber() {
        assertThat(validator.isValid("07873738547", context)).isTrue();
    }

    @Test
    public void shouldBeValidForValidMobileNumberWithSpaces() {
        assertThat(validator.isValid("0 787 373 8547", context)).isTrue();
    }

    @Test
    public void shouldBeValidForValidMobileNumberWithDashes() {
        assertThat(validator.isValid("0-787-373-8547", context)).isTrue();
    }

    @Test
    public void shouldBeValidForValidMobileNumberWithParens() {
        assertThat(validator.isValid("(0) 7873738547", context)).isTrue();
    }

    @Test
    public void shouldBeInvalidForValidMobileNumberWithIllegalCharacters() {
        assertThat(validator.isValid("#07873738547", context)).isFalse();
    }

    @Test
    public void shouldBeInvalidForMobileNumberStartingWithDoubleZeroes() {
        assertThat(validator.isValid("007873738547", context)).isFalse();
    }

    @Test
    public void shouldBeInvalidForTooShortMobileNumber() {
        assertThat(validator.isValid("078737385", context)).isFalse();
    }

    @Test
    public void shouldBeInvalidForTooLongMobileNumber() {
        assertThat(validator.isValid("078737385478", context)).isFalse();
    }

    @Test
    public void shouldBeInvalidForGarbageString() {
        assertThat(validator.isValid("!@#$%^&*$$", context)).isFalse();
    }

    @Test
    public void shouldBeValidForMobileNumberWithDoubleZeroesCountryCode() {
        assertThat(validator.isValid("00447873738547", context)).isTrue();
    }

    @Test
    public void shouldBeValidForMobileNumberWithPlusCountryCode() {
        assertThat(validator.isValid("+447873738547", context)).isTrue();
    }

    @Test
    public void shouldBeInvalidForMobileNumberWithNonUKCountryCode() {
        assertThat(validator.isValid("+48 7873738547", context)).isFalse();
    }

}
