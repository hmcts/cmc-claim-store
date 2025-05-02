package uk.gov.hmcts.cmc.claimstore.constants;

import lombok.experimental.UtilityClass;

/**
 * Constants class to store response messages.
 */
@UtilityClass
public class ResponseConstants {

    public static final String UNPROCESSABLE_ENTITY_UNRECOGNISED_DEADLINE_TYPE
        = "Unprocessable Entity Unrecognised Deadline Type";

    public static final String CREATE_CLAIM_DISABLED = "MoneyClaims jurisdiction & case type are for citizens only, "
        + "and should not be chosen or used by legal reps."
        + "Citizens, when issuing a claim will use a different platform.";
}
