package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ClaimIssuedStaffNotificationHandlerTest {

    @MockBean
    private ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;

    @Test
    public void notifyStaff() {
        //given
        ClaimIssuedStaffNotificationHandler claimIssuedStaffNotificationHandler
            = new ClaimIssuedStaffNotificationHandler(claimIssuedStaffNotificationService);

        //when

        claimIssuedStaffNotificationHandler.notifyStaff(new DocumentGeneratedEvent());

        //then
    }
}
