package uk.gov.hmcts.cmc.launchdarkly.internal;

import com.launchdarkly.client.LDClient;
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
        LDClient client = factory.create("test key");
        assertNotNull(client);
    }
}
