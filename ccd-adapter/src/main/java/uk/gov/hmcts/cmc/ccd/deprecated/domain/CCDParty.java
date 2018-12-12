package uk.gov.hmcts.cmc.ccd.deprecated.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDParty {
    private CCDPartyType type;
    private String email;
    private CCDAddress serviceAddress;
    private CCDIndividual individual;
    private CCDCompany company;
    private CCDOrganisation organisation;
    private CCDSoleTrader soleTrader;

}
