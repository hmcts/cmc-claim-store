package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CCJRequestSubmittedNotificationEmailContentProviderTest {

    private static final String EMAIL_SUBJECT = "Email Subject";
    private static final String EMAIL_BODY = "Email Body";
    private CCJRequestSubmittedNotificationEmailContentProvider provider;
    @Captor
    private ArgumentCaptor<Map<String, Object>> mapCaptor;

    @Mock
    private TemplateService templateService;

    @Mock
    private StaffEmailTemplates staffEmailTemplates;

    @Before
    public void setup() {
        provider = new CCJRequestSubmittedNotificationEmailContentProvider(templateService, staffEmailTemplates);

        when(staffEmailTemplates.getCCJRequestSubmittedEmailSubject()).thenReturn(EMAIL_SUBJECT);
        when(staffEmailTemplates.getCCJRequestSubmittedEmailBody()).thenReturn(EMAIL_BODY);
    }

    @Test(expected = NullPointerException.class)
    public void givenInputIsNullThenshouldThrowNullPointer() {
        provider.createContent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenInputMapIsEmptyThenshouldThrowIllegalArgument() {
        provider.createContent(Collections.emptyMap());
    }

    @Test
    public void shouldCreateValidContent() {
        Map<String, Object> input = new HashMap<>();
        input.put("test","test");
        provider.createContent(input);
        verify(templateService, times(2)).evaluate(any(), mapCaptor.capture());
        assertThat(input.entrySet().containsAll(mapCaptor.getValue().entrySet()));
    }

}
