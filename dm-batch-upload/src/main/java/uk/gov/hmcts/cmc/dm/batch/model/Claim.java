package uk.gov.hmcts.cmc.dm.batch.model;

import lombok.Data;

@Data
public class Claim {

    private Long id;
    private String externalId;
    private String referenceNumber;

}
