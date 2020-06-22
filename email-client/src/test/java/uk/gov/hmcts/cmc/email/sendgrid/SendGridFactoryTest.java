package uk.gov.hmcts.cmc.email.sendgrid;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SendGridFactoryTest {
    private SendGridFactory factory;

    @Before
    public void setUp() {
        factory = new SendGridFactory();
    }

    @Test
    public void testCreate() {
        assertNotNull(factory.createSendGrid("api key", true));
    }
}
