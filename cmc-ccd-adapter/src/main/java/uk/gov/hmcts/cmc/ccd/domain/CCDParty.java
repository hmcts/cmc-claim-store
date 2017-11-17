package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CCDParty {
    private final CCDPartyType type;
    private final String email;
    private final CCDAddress serviceAddress;
    private final CCDIndividual individual;
    private final CCDCompany company;
    private final CCDOrganisation organisation;
    private final CCDSoleTrader soleTrader;

}
