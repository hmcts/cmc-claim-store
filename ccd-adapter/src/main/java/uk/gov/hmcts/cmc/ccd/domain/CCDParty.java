package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
public class CCDParty {
    private String partyId;
    private String idamId;
    private CCDPartyType type;
    private String title;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private CCDAddress primaryAddress;
    private String emailAddress;
    private CCDTelephone telephoneNumber;
    private CCDAddress correspondenceAddress;
    private String businessName;
    private String contactPerson;
    private String companiesHouseNumber;
}
