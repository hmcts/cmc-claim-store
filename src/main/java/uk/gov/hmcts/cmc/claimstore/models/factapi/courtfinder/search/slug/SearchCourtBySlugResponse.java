package uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.slug;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Address;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.AreaOfLaw;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Facilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchCourtBySlugResponse {

    private List<Address> addresses;

    @JsonProperty("areas_of_law")
    private List<AreaOfLaw> areasOfLaw;

    private List<Facilities> facilities;

    private String name;

    private String slug;

    @JsonSetter("courtAddresses")
    public void readCourtAddresses(List<FactAddress> courtAddresses) {
        addresses = courtAddresses == null ? List.of() : courtAddresses.stream()
            .map(address -> Address.builder()
                .addressLines(Stream.of(address.addressLine1, address.addressLine2)
                    .filter(Objects::nonNull).filter(line -> !line.isBlank()).toList())
                .town(address.townCity)
                .postcode(address.postcode)
                .type(addressType(address.addressType))
                .build())
            .toList();
    }

    private static String addressType(String type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case "WRITE_TO_US":
                return "Write to us";
            case "VISIT_US":
                return "Visit us";
            case "VISIT_OR_CONTACT_US":
                return "Visit or contact us";
            default:
                return type;
        }
    }

    @JsonSetter("courtAreasOfLaw")
    public void readCourtAreasOfLaw(List<FactAreasOfLaw> courtAreasOfLaw) {
        areasOfLaw = courtAreasOfLaw == null ? List.of() : courtAreasOfLaw.stream()
            .filter(area -> area.areasOfLaw != null)
            .flatMap(area -> area.areasOfLaw.stream())
            .map(area -> AreaOfLaw.builder().name(area.name).displayName(area.displayName)
                .externalLink(area.externalLink).build())
            .toList();
    }

    @JsonSetter("courtFacilities")
    public void readCourtFacilities(List<JsonNode> courtFacilities) {
        facilities = new ArrayList<>();
        if (courtFacilities == null) {
            return;
        }
        for (JsonNode courtFacility : courtFacilities) {
            addFacility(courtFacility, "parking", "Parking");
            addFacility(courtFacility, "freeWaterDispensers", "Free water dispensers");
            addFacility(courtFacility, "snackVendingMachines", "Snack vending machines");
            addFacility(courtFacility, "drinkVendingMachines", "Drink vending machines");
            addFacility(courtFacility, "cafeteria", "Cafeteria");
            addFacility(courtFacility, "waitingArea", "Waiting area");
            addFacility(courtFacility, "waitingAreaChildren", "Children's waiting area");
            addFacility(courtFacility, "quietRoom", "Quiet room");
            addFacility(courtFacility, "babyChanging", "Baby changing");
            addFacility(courtFacility, "wifi", "WiFi");
        }
    }

    private void addFacility(JsonNode courtFacility, String field, String name) {
        if (courtFacility.path(field).asBoolean(false)) {
            facilities.add(Facilities.builder().name(name).order(facilities.size()).build());
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactAddress {
        private String addressLine1;
        private String addressLine2;
        private String townCity;
        private String postcode;
        private String addressType;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactAreasOfLaw {
        private List<FactAreaOfLaw> areasOfLaw;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactAreaOfLaw {
        private String name;
        private String displayName;
        private String externalLink;
    }
}
