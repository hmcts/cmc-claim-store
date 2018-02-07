package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDPartyArrayElement {
    private String id;
    private CCDParty value;
}
