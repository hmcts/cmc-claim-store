package uk.gov.hmcts.cmc.claimstore.stereotypes;

import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;

/**
 * Intended for classes which create content for email sent via SMTP server.
 *
 * @param <T> the type of object which carries data that populates the email content
 */
@SuppressWarnings("squid:S1609") // Not a functional interface
public interface EmailContentProvider<T> {

    EmailContent createContent(T input);

}
