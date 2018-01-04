package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleFullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleResponse;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.BeanValidator.validate;

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
    public void shouldHaveNoValidationMessagesWhenPartialResponseDataIsValid() {
        //given
        Response responseData = SampleResponse.validPartAdmissionDefaults();
        //when
        Set<ConstraintViolation<Response>> response = validator.validate(responseData);
        //then
        assertThat(response).isEmpty();
    }

    @Test
    public void shouldHaveValidationMessagesWhenPartialResponseDataElementsAreInvalid() {
        //given
        Response response = SamplePartAdmissionResponse.builder()
            .withPartAdmissionType(null)
            .withThatMuchOwed(null)
            .withEvidence(null)
            .withTimeline(null)
            .withDefendantPaymentPlan(null)
            .withImpactOfDispute("")
            .build();

        //when
        Set<String> errors = validate(response);

        //then
        assertThat(errors)
            .containsExactlyInAnyOrder(
                "partAdmissionType : may not be null",
                "howMuchOwed : may not be null",
                "timeline : may not be null",
                "evidence : may not be null",
                "impactOfDispute : may not be empty"
            );
    }

    @Test
    public void shouldHaveValidationMessagesWhenResponseDataElementsAreInValid() {
        //given
        Response response = SampleFullDefenceResponse.builder()
            .withDefence(null)
            .withDefenceType(null)
            .build();

        //when
        Set<String> errors = validate(response);

        //then
        assertThat(errors)
            .hasSize(2)
            .contains(
                "defenceType : may not be null",
                "defence : may not be empty"
            );
    }


    @Test
    public void shouldHaveValidationMessagesWhenDefenceDataElementIsEmpty() {
        //given
        Response response = SampleFullDefenceResponse.builder()
            .withDefence("")
            .withDefenceType(null)
            .build();

        //when
        Set<String> errors = validate(response);

        //then
        assertThat(errors)
            .hasSize(2)
            .contains(
                "defence : may not be empty",
                "defenceType : may not be null"
            );
    }

    @Test
    public void shouldHaveValidationMessagesWhenDefenceExceedsSizeLimit() {
        //given
        String defence = new ResourceReader().read("/defence_exceeding_size_limit.text");

        Response response = SampleFullDefenceResponse.builder()
            .withDefence(defence)
            .withDefenceType(null)
            .build();

        //when
        Set<String> errors = validate(response);

        //then
        assertThat(errors)
            .hasSize(2)
            .contains(
                "defence : size must be between 0 and 99000",
                "defenceType : may not be null"
            );
    }
}
