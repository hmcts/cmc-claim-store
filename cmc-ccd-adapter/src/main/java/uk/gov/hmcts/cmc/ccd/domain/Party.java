package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Party {
    private final PartyType type;
    private final String email;
    private final Address serviceAddress;
    private final Individual individual;
    private final Company company;
    private final Organisation organisation;
    private final SoleTrader soleTrader;

}
