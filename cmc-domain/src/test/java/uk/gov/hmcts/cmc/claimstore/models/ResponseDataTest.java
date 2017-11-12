package uk.gov.hmcts.cmc.claimstore.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleResponseData;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.BeanValidator.validate;

public class ResponseDataTest {
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    public void shouldHaveNoValidationMessagesWhenResponseDataIsValid() {
        //given
        ResponseData responseData = SampleResponseData.validDefaults();
        //when
        Set<ConstraintViolation<ResponseData>> response = validator.validate(responseData);
        //then
        assertThat(response).isEmpty();
    }

    @Test
    public void shouldHaveValidationMessagesWhenResponseDataElementsAreInValid() {
        //given
        ResponseData responseData = SampleResponseData.builder()
            .withDefence(null)
            .withMediation(null)
            .withResponseType(null)
            .build();

        //when
        Set<String> errors = validate(responseData);

        //then
        assertThat(errors)
            .hasSize(3)
            .contains(
                "type : may not be null",
                "defence : may not be empty",
                "freeMediation : may not be null"
            );
    }


    @Test
    public void shouldHaveValidationMessagesWhenDefenceDataElementIsEmpty() {
        //given
        ResponseData responseData = SampleResponseData.builder()
            .withDefence("")
            .withMediation(null)
            .withResponseType(null)
            .build();

        //when
        Set<String> errors = validate(responseData);

        //then
        assertThat(errors)
            .hasSize(3)
            .contains(
                "type : may not be null",
                "defence : may not be empty",
                "freeMediation : may not be null"
            );
    }

    @Test
    public void shouldHaveValidationMessagesWhenDefenceExceedsSizeLimit() {
        //given
        final String defence = new ResourceReader().read("/defence_exceeding_size_limit.text");

        ResponseData responseData = SampleResponseData.builder()
            .withDefence(defence).withMediation(null).withResponseType(null).build();

        //when
        Set<String> errors = validate(responseData);

        //then
        assertThat(errors)
            .hasSize(3)
            .contains(
                "defence : size must be between 0 and 99000",
                "type : may not be null",
                "freeMediation : may not be null"
            );
    }
}
