package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import uk.gov.hmcts.cmc.claimstore.healthcheck.GovNotifyHealthIndicator;
import uk.gov.hmcts.cmc.claimstore.healthcheck.PDFServiceHealthIndicator;
import uk.gov.hmcts.reform.docassembly.healthcheck.DocAssemblyHealthIndicator;
import uk.gov.hmcts.reform.document.healthcheck.DocumentManagementHealthIndicator;
import uk.gov.hmcts.reform.sendletter.healthcheck.SendLetterHealthIndicator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckControllerTest {

    @InjectMocks
    private HealthCheckController healthCheckController;

    @Mock
    GovNotifyHealthIndicator govNotifyHealthIndicator;

    @Mock
    SendLetterHealthIndicator sendLetterHealthIndicator;

    @Mock
    PDFServiceHealthIndicator pdfServiceHealthIndicator;

    @Mock
    DocAssemblyHealthIndicator docAssemblyHealthIndicator;

    @Mock
    DocumentManagementHealthIndicator documentManagementHealthIndicator;

    @Before
    public void setup() {
        when(govNotifyHealthIndicator.health()).thenReturn(Health.up().build());
        when(sendLetterHealthIndicator.health()).thenReturn(Health.up().build());
        when(pdfServiceHealthIndicator.health()).thenReturn(Health.up().build());
        when(docAssemblyHealthIndicator.health()).thenReturn(Health.up().build());
        when(documentManagementHealthIndicator.health()).thenReturn(Health.up().build());
    }

    @Test
    public void shouldReturnTrueIfAllTheDependentServicesAreUp() {
        assertTrue(healthCheckController.check());
    }

    @Test
    public void shouldReturnFalseIfAnyDependentServiceIsDown() {
        when(govNotifyHealthIndicator.health()).thenReturn(Health.down().build());
        assertFalse(healthCheckController.check());
    }

}
