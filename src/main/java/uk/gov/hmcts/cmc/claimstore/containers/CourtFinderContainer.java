package uk.gov.hmcts.cmc.claimstore.containers;

import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
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
 * Court Finder Container for mapping court finder responses
 */
public class CourtFinderContainer {

    private final CourtFinderApi courtFinderApi;

    public CourtFinderContainer(CourtFinderApi courtFinderApi) {
        this.courtFinderApi = courtFinderApi;
    }

    /**
     * Gets a list of court objects from the CourtFinderResponse
     *
     * @param courtFinderResponse : for a given CourtFinderResponse
     * @return {@linkplain  List<Court>}
     */
    public List<Court> getCourtsFromCourtFinderResponse(CourtFinderResponse courtFinderResponse) {
        List<Court> courts = new ArrayList<>();

        for (uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.Court court : courtFinderResponse.getCourts()) {
            courts.add(getCourtFromCourtFinderResponse(court));
        }

        return courts;
    }

    /**
     * Get a court object from the fact api court object
     *
     * @param court : for a given court object
     * @return {@linkplain Court}
     */
    public Court getCourtFromCourtFinderResponse(uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.Court court) {
        Court courtItem = new Court();
        CourtDetails courtDetails = courtFinderApi.getCourtDetailsFromNameSlug(court.getSlug());

        courtItem.setName(court.getName());
        courtItem.setSlug(court.getSlug());
        populateCourtAddresses(courtItem, courtDetails.getAddresses());
        courtItem.setAreasOfLaw(getAreaOfLawFromCourtFinderCourt(court));

        return courtItem;
    }

    /**
     * Method to populate the court addresses
     *
     * @param courtItem for a provided court item
     * @param courtAddresses for a provided list of court addresses
     */
    private void populateCourtAddresses(Court courtItem, List<CourtAddress> courtAddresses) {
        List<Address> addresses = new ArrayList<>();
        Map<String, Address> courtAddressMap = new HashMap<>();

        for (CourtAddress courtAddress : courtAddresses) {
            Address address =  new Address();
            address.setAddressLines(courtAddress.getAddressLines());
            address.setPostcode(courtAddress.getPostcode());
            address.setTown(courtAddress.getTown());
            address.setType(courtAddress.getType());
            courtAddressMap.put(courtAddress.getType(), address);
            addresses.add(address);
        }

        setCourtAddress(courtItem, courtAddressMap);
        courtItem.setAddresses(addresses);
    }

    /**
     * Method for setting the court address in order of priority
     *
     * @param courtItem for a provided court item
     * @param courtAddressMap for a map that contains the address type and the matching address
     */
    private void setCourtAddress(Court courtItem, Map<String, Address> courtAddressMap) {
        if (courtAddressMap.containsKey("Write to us")) {
            courtItem.setAddress(courtAddressMap.get("Write to us"));
        } else if (courtAddressMap.containsKey("Visit or Contact Us")) {
            courtItem.setAddress(courtAddressMap.get("Visit or Contact Us"));
        } else if (courtAddressMap.containsKey("Visit Us")) {
            courtItem.setAddress(courtAddressMap.get("Visit Us"));
        }
    }

    /**
     * Gets the AreaOfLaw From CourtFinderResponse
     *
     * @param court for a provide fact api court object
     * @return {@linkplain List < AreaOfLaw >}
     */
    private List<AreaOfLaw> getAreaOfLawFromCourtFinderCourt(uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.Court court) {
        List<AreaOfLaw> areasOfLaws = new ArrayList<>();

        for (AreasOfLaw areasOfLawSpoe : court.getAreasOfLawSpoe()) {
            AreaOfLaw areaOfLaw = new AreaOfLaw();
            areaOfLaw.setName(areasOfLawSpoe.getName());
            areasOfLaws.add(areaOfLaw);
        }

        return areasOfLaws;
    }

}
