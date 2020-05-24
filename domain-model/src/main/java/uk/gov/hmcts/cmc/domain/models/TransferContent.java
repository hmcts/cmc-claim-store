package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@EqualsAndHashCode
@Getter
public class TransferContent {

    private LocalDate dateOfTransfer;
    private String reasonForTransfer;
    private String nameOfTransferCourt;
    private Address addressOfTransferCourt;
}
