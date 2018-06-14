package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.AddressDiff;
import uk.gov.hmcts.cmc.claimstore.services.DefendantAddressDiffer;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.HashMap;
import java.util.Map;

@Service
public class DefendantAddressChangeStaffNotificationService {
    private final EmailService emailService;
    private final StaffEmailProperties staffEmailProperties;
    private final ClaimIssuedStaffNotificationEmailContentProvider provider;

    @Autowired
    public DefendantAddressChangeStaffNotificationService (
        EmailService emailService,
        StaffEmailProperties staffEmailProperties,
        ClaimIssuedStaffNotificationEmailContentProvider provider
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.provider = provider;
    }

    public void notifyStaffIfDefendantAddressChanged(Claim claim, Response response)
    {
        DefendantAddressDiffer differ = new DefendantAddressDiffer(claim, response);
        AddressDiff diff = differ.getDiff();

        if(diff.isEmpty()) {
            return;
        }

        EmailData emailData = prepareEmailData(diff);

        emailService.sendEmail(staffEmailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(AddressDiff diff) {
        EmailContent content = provider.createContent(wrapInMap(diff));
        /*
        List<EmailAttachment> attachments = documents.stream()
            .map(document -> pdf(document.getBytes(), document.getFilename()))
            .collect(Collectors.toList());
        */

        return new EmailData(staffEmailProperties.getRecipient(),
            content.getSubject(),
            content.getBody(),
            //todo need to attach json of change?
            null);
    }


    private static Map<String, Object> wrapInMap(AddressDiff diff) {
        Map<String, Object> map = new HashMap<>();
        map.put("addressDiff", diff.getAddressDiff());
        map.put("correspondenceAddressDiff", diff.getCorrespondenceAddressDiff());
        return map;
    }
}
