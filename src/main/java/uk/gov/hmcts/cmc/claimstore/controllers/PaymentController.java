package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
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
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Payment update callback was processed successfully and updated to the case",
            response = UpdatePaymentResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
        @ApiResponse(code = 403, message = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<UpdatePaymentResponse> updateCardPayment(
        @RequestHeader(value = SERVICE_AUTHORIZATION_HEADER) String serviceToken,
        @Valid @NotNull @RequestBody PaymentUpdate paymentUpdate
    ) {
        logger.info("Called s2s service");
        try {
            logger.info("Payment Update - {}", paymentUpdate.toString());
            String serviceName = authTokenValidator.getServiceName("Bearer " + serviceToken);
            if ("payment_app".contains(serviceName)) {
                logger.info("Service Token Validated Successfully");
                claimService.updateCardPayment(paymentUpdate);
                return ResponseEntity.ok().build();
            } else {
                logger.info("Calling service is not authorised to use the endpoint");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (InvalidTokenException e) {
            logger.error(e.getMessage());
            logger.info("Provided S2S token is missing or invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
