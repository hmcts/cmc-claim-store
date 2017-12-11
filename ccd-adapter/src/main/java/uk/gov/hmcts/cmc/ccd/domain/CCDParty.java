package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class CCDParty {
    private CCDPartyType type;
    private String email;
    private CCDAddress serviceAddress;
    private CCDIndividual individual;
    private CCDCompany company;
    private CCDOrganisation organisation;
    private CCDSoleTrader soleTrader;

}
