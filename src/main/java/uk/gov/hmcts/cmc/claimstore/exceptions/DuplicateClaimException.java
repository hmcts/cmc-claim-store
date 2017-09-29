package uk.gov.hmcts.cmc.claimstore.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Duplicate claim")
public class DuplicateClaimException extends RuntimeException {

    public DuplicateClaimException(String message) {
        super(message);
    }

}
