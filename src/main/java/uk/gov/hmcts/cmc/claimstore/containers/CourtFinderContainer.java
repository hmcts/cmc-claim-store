package uk.gov.hmcts.cmc.claimstore.containers;

import uk.gov.hmcts.cmc.claimstore.constants.CourtAddressType;
import uk.gov.hmcts.cmc.claimstore.courtfinder.LegacyCourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Address;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.AreaOfLaw;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.CourtDetails;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.AreasOfLaw;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.CourtAddress;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.CourtFinderResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Court Finder Container for mapping court finder responses.
 */
public class CourtFinderContainer {

    private final LegacyCourtFinderApi legacyCourtFinderApi;

    public CourtFinderContainer(LegacyCourtFinderApi legacyCourtFinderApi) {
        this.legacyCourtFinderApi = legacyCourtFinderApi;
    }

    /**
     * Gets a list of court objects from the CourtFinderResponse.
     *
     * @param courtFinderResponse : for a given CourtFinderResponse
     * @return {@linkplain List}
     */
    public List<Court> getCourtsFromCourtFinderResponse(CourtFinderResponse courtFinderResponse) {
        List<Court> courts = new ArrayList<>();

        for (uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.Court court : courtFinderResponse.getCourts()) {
            courts.add(getCourtFromCourtFinderResponse(court));
        }

        return courts;
    }

    /**
     * Get a court object from the fact api court object.
     *
     * @param court : for a given court object
     * @return {@linkplain Court}
     */
    public Court getCourtFromCourtFinderResponse(uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.Court court) {
        Court courtItem = new Court();
        courtItem.setName(court.getName());
        courtItem.setSlug(court.getSlug());
        courtItem.setAreasOfLaw(getAreaOfLawFromCourtFinderCourt(court));

        if (court.getSlug() != null) {
            CourtDetails courtDetails = legacyCourtFinderApi.getCourtDetailsFromNameSlug(court.getSlug());

            if (courtDetails != null) {
                populateCourtAddresses(courtItem, courtDetails.getAddresses());
            }
        }

        return courtItem;
    }

    /**
     * Method to populate the court addresses.
     *
     * @param courtItem for a provided court item
     * @param courtAddresses for a provided list of court addresses
     */
    private void populateCourtAddresses(Court courtItem, List<CourtAddress> courtAddresses) {
        List<Address> addresses = new ArrayList<>();
        Map<String, Address> courtAddressMap = new HashMap<>();

        for (CourtAddress courtAddress : courtAddresses) {
            Address address = Address.builder()
                .addressLines(courtAddress.getAddressLines())
                .postcode(courtAddress.getPostcode())
                .town(courtAddress.getTown())
                .type(courtAddress.getType())
                .build();

            courtAddressMap.put(courtAddress.getType(), address);
            addresses.add(address);
        }

        setCourtAddress(courtItem, courtAddressMap);
        courtItem.setAddresses(addresses);
    }

    /**
     * Method for setting the court address in order of priority.
     *
     * @param courtItem for a provided court item
     * @param courtAddressMap for a map that contains the address type and the matching address
     */
    private void setCourtAddress(Court courtItem, Map<String, Address> courtAddressMap) {
        if (courtAddressMap.containsKey(CourtAddressType.WRITE_TO_US.toString())) {
            courtItem.setAddress(courtAddressMap.get(CourtAddressType.WRITE_TO_US.toString()));
        } else if (courtAddressMap.containsKey(CourtAddressType.VISIT_OR_CONTACT_US.toString())) {
            courtItem.setAddress(courtAddressMap.get(CourtAddressType.VISIT_OR_CONTACT_US.toString()));
        } else if (courtAddressMap.containsKey(CourtAddressType.VISIT_US.toString())) {
            courtItem.setAddress(courtAddressMap.get(CourtAddressType.VISIT_US.toString()));
        }
    }

    /**
     * Gets the AreaOfLaw From CourtFinderResponse.
     *
     * @param court for a provide fact api court object
     * @return {@linkplain List < AreaOfLaw >}
     */
    private List<AreaOfLaw> getAreaOfLawFromCourtFinderCourt(
        uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.Court court) {
        List<AreaOfLaw> areasOfLaws = new ArrayList<>();

        for (AreasOfLaw areasOfLawSpoe : court.getAreasOfLawSpoe()) {
            AreaOfLaw areaOfLaw = new AreaOfLaw();
            areaOfLaw.setName(areasOfLawSpoe.getName());
            areasOfLaws.add(areaOfLaw);
        }

        return areasOfLaws;
    }

}
