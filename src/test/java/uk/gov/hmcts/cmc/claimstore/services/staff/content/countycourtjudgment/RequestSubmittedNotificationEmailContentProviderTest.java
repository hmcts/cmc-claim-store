package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class RequestSubmittedNotificationEmailContentProviderTest {

    private RequestSubmittedNotificationEmailContentProvider provider;

    @Mock
    private TemplateService templateService;

    @Mock
    private StaffEmailTemplates emailTemplates;

    @Before
    public void setup() {
        provider = new RequestSubmittedNotificationEmailContentProvider(templateService, emailTemplates);
    }

    @Test(expected = NullPointerException.class)
    public void givenInputIsNullThenshouldThrowNullPointer() {
        provider.createContent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenInputMapIsEmptyThenshouldThrowIllegalArgument() {
        provider.createContent(Collections.emptyMap());
    }

}
