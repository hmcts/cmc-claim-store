package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CallbackTypeTest {

    @Test
    public void shouldDeserialiseValidCallbacks() {
        assertThat(CallbackType.fromValue("mid")).isEqualTo(CallbackType.MID);
        assertThat(CallbackType.fromValue("about-to-start"))
            .isEqualTo(CallbackType.ABOUT_TO_START);
        assertThat(CallbackType.fromValue("about-to-submit"))
            .isEqualTo(CallbackType.ABOUT_TO_SUBMIT);
        assertThat(CallbackType.fromValue("submitted"))
            .isEqualTo(CallbackType.SUBMITTED);
    }

    @Test(expected = CallbackException.class)
    public void shouldThrowIfUnknownCallback() {
        CallbackType.fromValue("nope");
    }
}
