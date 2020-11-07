package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.PaymentUpdate;
import uk.gov.hmcts.cmc.domain.models.paymentresponse.UpdatePaymentResponse;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping(
    path = "/payment",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentController {

    private final ClaimService claimService;

    private final AuthTokenValidator authTokenValidator;

    public static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    private final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    public PaymentController(ClaimService claimService, AuthTokenValidator authTokenValidator) {
        this.claimService = claimService;
        this.authTokenValidator = authTokenValidator;
    }

    @PutMapping(value = "/payment-update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Update a Card payment")
    public ResponseEntity<UpdatePaymentResponse> updateCardPayment(
        @RequestHeader(value = SERVICE_AUTHORIZATION_HEADER) String serviceToken,
        @Valid @NotNull @RequestBody PaymentUpdate paymentUpdate
    ) {
        logger.info("Called s2s service");
        try {
            logger.info("Payment Update - {0}", paymentUpdate.toString());
            String serviceName = authTokenValidator.getServiceName("Bearer " + serviceToken);
            if ("payment_app".contains(serviceName)) {
                logger.info("token validated {0}", serviceToken);
                claimService.updateCardPayment(paymentUpdate);
                return ResponseEntity.ok().build();
            } else {
                logger.info("Invalid Token");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
