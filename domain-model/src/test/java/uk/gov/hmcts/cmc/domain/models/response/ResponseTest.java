package uk.gov.hmcts.cmc.domain.models.response;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class ResponseTest {
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    public void shouldHaveNoValidationMessagesWhenResponseDataIsValid() {
        //given
        Response responseData = SampleResponse.validDefaults();
        //when
        Set<ConstraintViolation<Response>> response = validator.validate(responseData);
        //then
        assertThat(response).isEmpty();
    }

    @Test
    public void shouldHaveValidationMessagesWhenResponseDataElementsAreInValid() {
        //given
        Response response = SampleResponse.FullDefence.builder()
            .withDefenceType(null)
            .build();

        //when
        Set<String> errors = validate(response);

        //then
        assertThat(errors)
            .hasSize(1)
            .contains(
                "defenceType : may not be null"
            );
    }

    @Test
    public void shouldHaveValidationMessagesWhenDefenceIsEmpty() {
        //given
        Response response = SampleResponse.FullDefence.builder()
            .withDefence("")
            .build();

        //when
        Set<String> errors = validate(response);

        //then
        assertThat(errors)
            .hasSize(1)
            .contains(
                "defence : size must be between 1 and 99000"
            );
    }

    @Test
    public void shouldHaveValidationMessagesWhenDefenceExceedsSizeLimit() {
        //given
        Response response = SampleResponse.FullDefence.builder()
            .withDefence(randomAlphanumeric(99001))
            .build();

        //when
        Set<String> errors = validate(response);

        //then
        assertThat(errors)
            .hasSize(1)
            .contains(
                "defence : size must be between 1 and 99000"
            );
    }

    @Test
    public void shouldHaveValidationErrorWhenMediationPhoneNumberExceedSizeLimit() {
        //given
        Response response = SampleResponse.FullDefence.builder()
            .withMediationPhoneNumber(RandomStringUtils.randomAlphabetic(31))
            .build();

        //when
        Set<String> errors = validate(response);

        //then
        assertThat(errors)
            .hasSize(1)
            .contains(
                "mediationPhoneNumber : Mediation phone number may not be longer than 30 characters"
            );
    }

    @Test
    public void shouldHaveValidationErrorWhenMediationContactPersonExceedSizeLimit() {
        //given
        Response response = SampleResponse.FullDefence.builder()
            .withMediationContactPerson(RandomStringUtils.randomAlphabetic(31))
            .build();

        //when
        Set<String> errors = validate(response);

        //then
        assertThat(errors)
            .hasSize(1)
            .contains(
                "mediationContactPerson : Mediation contact person may not be longer than 30 characters"
            );
    }

    @Test
    public void shouldHaveValidationErrorWhenNoMediationReasonPersonExceedSizeLimit() {
        //given
        Response response = SampleResponse.FullDefence.builder()
            .withNoMediationReason(RandomStringUtils.randomAlphabetic(551))
            .build();

        //when
        Set<String> errors = validate(response);

        //then
        assertThat(errors)
            .hasSize(1)
            .contains(
                "noMediationReason : No mediation reason may not be longer than 550 characters"
            );
    }
}
