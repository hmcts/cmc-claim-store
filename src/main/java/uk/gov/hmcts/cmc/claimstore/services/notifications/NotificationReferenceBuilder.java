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
        String template,
        String toWhom,
        String claimReferenceNumber
    ) {
        return String.format(template, toWhom, claimReferenceNumber);
    }

    private static String reference(
        String template,
        String toWhom,
        String byWhom,
        String claimReferenceNumber
    ) {
        return String.format(template, toWhom, byWhom, claimReferenceNumber);
    }

    public static class MoreTimeRequested {

        public static final String TEMPLATE = "more-time-requested-notification-to-%s-%s";

        private MoreTimeRequested() {
            // do not instantiate
        }

        public static String referenceForDefendant(String claimReferenceNumber) {
            return reference(TEMPLATE, DEFENDANT, claimReferenceNumber);
        }
    }

    public static class ResponseSubmitted {

        public static final String TEMPLATE = "%s-response-notification-%s";

        private ResponseSubmitted() {
            // do not instantiate
        }

        public static String referenceForClaimant(String claimReferenceNumber) {
            return reference(TEMPLATE, CLAIMANT, claimReferenceNumber);
        }

        public static String referenceForDefendant(String claimReferenceNumber) {
            return reference(TEMPLATE, DEFENDANT, claimReferenceNumber);
        }
    }

    public static class ClaimantResponseSubmitted {

        public static final String TEMPLATE = "to-%s-claimant’s-response-submitted-notification-%s";

        private ClaimantResponseSubmitted() {
            // do not instantiate
        }

        public static String referenceForClaimant(String claimReferenceNumber) {
            return reference(TEMPLATE, CLAIMANT, claimReferenceNumber);
        }

        public static String referenceForDefendant(String claimReferenceNumber) {
            return reference(TEMPLATE, DEFENDANT, claimReferenceNumber);
        }
    }

    public static class CCJRequested {

        private CCJRequested() {
            // do not instantiate
        }

        public static String referenceForClaimant(String claimReferenceNumber) {
            return reference("%s-ccj-requested-notification-%s", CLAIMANT, claimReferenceNumber);
        }
    }

    public static class CCJIssued {

        private CCJIssued() {
            // do not instantiate
        }

        public static String referenceForDefendant(String claimReferenceNumber) {
            return reference("%s-ccj-issued-notification-%s", CLAIMANT, claimReferenceNumber);
        }
    }

    public static class OfferMade {

        public static final String TEMPLATE = "%s-offer-made-notification-%s";

        private OfferMade() {
            // do not instantiate
        }

        public static String referenceForClaimant(String claimReferenceNumber) {
            return reference(TEMPLATE, CLAIMANT, claimReferenceNumber);
        }

        public static String referenceForDefendant(String claimReferenceNumber) {
            return reference(TEMPLATE, DEFENDANT, claimReferenceNumber);
        }
    }

    public static class OfferAccepted {

        public static final String TEMPLATE = "to-%s-offer-accepted-by-claimant-notification-%s";

        private OfferAccepted() {
            // do not instantiate
        }

        public static String referenceForClaimant(String claimReferenceNumber) {
            return reference(TEMPLATE, CLAIMANT, claimReferenceNumber);
        }

        public static String referenceForDefendant(String claimReferenceNumber) {
            return reference(TEMPLATE, DEFENDANT, claimReferenceNumber);
        }
    }

    public static class OfferRejected {

        public static final String TEMPLATE = "to-%s-offer-rejected-by-claimant-notification-%s";

        private OfferRejected() {
            // do not instantiate
        }

        public static String referenceForClaimant(String claimReferenceNumber) {
            return reference(TEMPLATE, CLAIMANT, claimReferenceNumber);
        }

        public static String referenceForDefendant(String claimReferenceNumber) {
            return reference(TEMPLATE, DEFENDANT, claimReferenceNumber);
        }
    }

    public static class AgreementCounterSigned {

        public static final String TEMPLATE = "to-%s-agreement-counter-signed-by-%s-notification-%s";

        private AgreementCounterSigned() {
            // do not instantiate
        }

        public static String referenceForClaimant(String claimReferenceNumber, String otherParty) {
            return reference(TEMPLATE, CLAIMANT, otherParty.toLowerCase(), claimReferenceNumber);
        }

        public static String referenceForDefendant(String claimReferenceNumber, String otherParty) {
            return reference(TEMPLATE, DEFENDANT, otherParty.toLowerCase(), claimReferenceNumber);
        }
    }

    public static class PaidInFull {

        private PaidInFull() {
            // do not instantiate
        }

        public static String referenceForDefendant(String claimReferenceNumber) {
            return reference("%s-paid-in-full-notification-%s", CLAIMANT, claimReferenceNumber);
        }
    }

    public static class RedeterminationRequested {
        private RedeterminationRequested() {
            // do not instantiate
        }

        public static String referenceForClaimant(String claimReferenceNumber) {
            return reference("%s-requested-redetermination-%s", DEFENDANT, claimReferenceNumber);
        }
    }

    public static class SettlementRejected {
        private SettlementRejected() {
            // do not instantiate
        }

        public static String referenceForClaimant(String claimReferenceNumber) {
            return reference("settlement-rejected-%s", CLAIMANT, claimReferenceNumber);
        }

        public static String referenceForDefendant(String claimReferenceNumber) {
            return reference("settlement-rejected-%s", DEFENDANT, claimReferenceNumber);
        }
    }

    public static class LegalOrderDrawn {
        private LegalOrderDrawn() {
            // do not instantiate
        }

        private static String referenceForClaim(String claimReferenceNumber, String party) {
            return reference("to-%s-legal-order-drawn-notification-%s", party, claimReferenceNumber);
        }

        public static String referenceForDefendant(String claimReferenceNumber) {
            return referenceForClaim(claimReferenceNumber, DEFENDANT);
        }

        public static String referenceForClaimant(String claimReferenceNumber) {
            return referenceForClaim(claimReferenceNumber, CLAIMANT);
        }
    }

    public static class MediationSuccessful {

        public static final String TEMPLATE = "to-%s-mediation-successful";

        private MediationSuccessful() {
            // do not instantiate
        }

        public static String referenceForClaimant(String claimReferenceNumber, String otherParty) {
            return reference(TEMPLATE, CLAIMANT, otherParty.toLowerCase(), claimReferenceNumber);
        }

        public static String referenceForDefendant(String claimReferenceNumber, String otherParty) {
            return reference(TEMPLATE, DEFENDANT, otherParty.toLowerCase(), claimReferenceNumber);
        }
    }

}
