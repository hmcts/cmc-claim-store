package uk.gov.hmcts.cmc.claimstore.constants;

/**
 * Enum class to contain court address types.
 */
public enum CourtAddressType {
    WRITE_TO_US("Write to us"),
    VISIT_OR_CONTACT_US("Visit or Contact Us"),
    VISIT_US("Visit Us");

    final String value;

    CourtAddressType(String value) {
        this.value = value;
    }

    public static boolean contains(String value) {

        for (CourtAddressType courtAddressType : CourtAddressType.values()) {
            if (courtAddressType.value.equals(value)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return value;
    }
}
