package uk.gov.hmcts.cmc.launchdarkly.internal;

import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LDClientFactoryTest {
    private LDClientFactory factory;

    @BeforeEach
    void setUp() {
        factory = new LDClientFactory();
    }

    @Test
    void testCreate() {
        LDClientInterface client = factory.create("test key", true);
        assertNotNull(client);
    }
}
