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

    private ReferenceNumberService referenceNumberService;

    @Mock
    private ReferenceNumberRepository referenceNumberRepository;

    @Before
    public void setUp() {
        referenceNumberService = new ReferenceNumberService(referenceNumberRepository);
    }

    @Test
    public void shouldGetReferenceNumberForCitizen() {
        String citizenReference = "000MC001";
        when(referenceNumberRepository.getReferenceNumberForCitizen()).thenReturn(citizenReference);

        String referenceNumber = referenceNumberService.getReferenceNumber(false);

        assertThat(referenceNumber).isNotNull().isEqualTo(citizenReference);
    }

    @Test
    public void shouldGetReferenceNumberForLegalRepresentative() {
        String legalReference = "000LR001";
        when(referenceNumberRepository.getReferenceNumberForLegal()).thenReturn(legalReference);

        String referenceNumber = referenceNumberService.getReferenceNumber(true);

        assertThat(referenceNumber).isNotNull().isEqualTo(legalReference);
    }
}
