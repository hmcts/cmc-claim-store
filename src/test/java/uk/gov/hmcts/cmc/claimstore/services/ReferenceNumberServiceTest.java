package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceNumberServiceTest {

    private static String CITIZEN_REFERENCE = "000MC001";
    private static String LEGAL_REFERENCE = "000LR001";

    private ReferenceNumberService referenceNumberService;

    @Mock
    private ReferenceNumberRepository referenceNumberRepository;

    @Before
    public void setUp() {
        referenceNumberService = new ReferenceNumberService(referenceNumberRepository);
    }

    @Test
    public void shouldGetReferenceNumberForCitizen() {

        when(referenceNumberRepository.getReferenceNumberForCitizen()).thenReturn(CITIZEN_REFERENCE);

        Boolean claimRepresented = false;
        String referenceNumber = referenceNumberService.getReferenceNumber(claimRepresented);

        assertThat(referenceNumber).isNotNull().isEqualTo(CITIZEN_REFERENCE);
    }

    @Test
    public void shouldGetReferenceNumberForLegalRepresentative() {

        when(referenceNumberRepository.getReferenceNumberForLegal()).thenReturn(LEGAL_REFERENCE);

        Boolean claimRepresented = true;
        String referenceNumber = referenceNumberService.getReferenceNumber(claimRepresented);

        assertThat(referenceNumber).isNotNull().isEqualTo(LEGAL_REFERENCE);
    }
}
