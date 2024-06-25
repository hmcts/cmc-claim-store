package uk.gov.hmcts.cmc.email.sendgrid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SendGridFactoryTest {
    private SendGridFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new SendGridFactory();
    }

    @Test
    public void testCreate() {
        assertNotNull(factory.createSendGrid("api key", true));
    }
}
