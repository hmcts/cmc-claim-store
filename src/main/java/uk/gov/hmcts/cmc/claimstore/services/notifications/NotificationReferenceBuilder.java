package uk.gov.hmcts.cmc.claimstore.services.notifications;

/**
 * Generates notification references.
 */
public class NotificationReferenceBuilder {

    public static final String CLAIMANT = "claimant";
    public static final String DEFENDANT = "defendant";

    private NotificationReferenceBuilder() {
        // do not instantiate
    }

    private static String reference(
        final String template,
        final String toWhom,
        final String claimReferenceNumber
    ) {
        return String.format(template, toWhom, claimReferenceNumber);
    }

    public static class MoreTimeRequested {

        public static final String TEMPLATE = "more-time-requested-notification-to-%s-%s";

        private MoreTimeRequested() {
            // do not instantiate
        }

        public static String referenceForDefendant(final String claimReferenceNumber) {
            return reference(TEMPLATE, DEFENDANT, claimReferenceNumber);
        }
    }

    public static class ResponseSubmitted {

        public static final String TEMPLATE = "%s-response-notification-%s";

        private ResponseSubmitted() {
            // do not instantiate
        }

        public static String referenceForClaimant(final String claimReferenceNumber) {
            return reference(TEMPLATE, CLAIMANT, claimReferenceNumber);
        }

        public static String referenceForDefendant(final String claimReferenceNumber) {
            return reference(TEMPLATE, DEFENDANT, claimReferenceNumber);
        }
    }
}
