package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SoleTrader {

    private final String title;
    private final String name;
    private final String businessName;
    private final String mobilePhone;
    private final Address address;
    private final Address correspondenceAddress;
    private final Representative representative;

}
