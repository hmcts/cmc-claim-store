package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.PaperResponseTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

@ExtendWith(MockitoExtension.class)
public class PaperResponseLetterServiceTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldCreateCoverLetter() {

    }

    @Test
    void shouldCreateOconFormForIndividualWithDQs() {
        //claim with dqs
        Claim claim = SampleClaim.getCitizenClaim();
        //check for correct template id and payload function being called?


    }
    @Test
    void shouldCreateOconFormForIndividualWithoutDQs() {

    }
    @Test
    void shouldCreateOconFormForSoleTraderWthDQs() {

    }
    @Test
    void shouldCreateOconFormForSoleTraderWithoutDQs() {

    }
    @Test
    void shouldCreateOconFormForCompanyWithDQs() {

    }
    @Test
    void shouldCreateOconFormForCompanyWithoutDQs() {

    }
}
