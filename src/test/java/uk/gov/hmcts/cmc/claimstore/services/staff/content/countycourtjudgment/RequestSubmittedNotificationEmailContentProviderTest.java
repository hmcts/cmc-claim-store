package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class RequestSubmittedNotificationEmailContentProviderTest {

    private RequestSubmittedNotificationEmailContentProvider provider;

    @Mock
    private TemplateService templateService;

    @Mock
    private StaffEmailTemplates emailTemplates;

    @BeforeEach
    public void setup() {
        provider = new RequestSubmittedNotificationEmailContentProvider(templateService, emailTemplates);
    }

    @Test
    public void givenInputIsNullThenshouldThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> {
            provider.createContent(null);
        });
    }

    @Test
    public void givenInputMapIsEmptyThenshouldThrowIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> {
            provider.createContent(Collections.emptyMap());
        });
    }

}
