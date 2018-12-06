package uk.gov.hmcts.cmc.ccd.jackson.mixin;

    import com.fasterxml.jackson.annotation.JsonProperty;

public interface SoleTraderMixIn extends PartyMixIn {

    @JsonProperty("partyTitle")
    String getTitle();

    @JsonProperty("partyBusinessName")
    String getBusinessName();
}
