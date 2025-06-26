package uk.gov.hmcts.cmc.claimstore.constants;

import lombok.experimental.UtilityClass;

/**
 * Constants class to store response messages.
 */
@UtilityClass
public class ResponseConstants {

    public static final String UNPROCESSABLE_ENTITY_UNRECOGNISED_DEADLINE_TYPE
        = "Unprocessable Entity Unrecognised Deadline Type";

    public static final String CREATE_CLAIM_DISABLED = "The Money Claims service is no longer available for issuing new specified claims. "
        + "Please use the Civil option available in the jurisdiction and case type drop down lists to issue a specified claim.";
}
