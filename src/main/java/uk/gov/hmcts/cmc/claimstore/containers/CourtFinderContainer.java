package uk.gov.hmcts.cmc.claimstore.containers;

import uk.gov.hmcts.cmc.claimstore.courtfinder.models.AreaOfLaw;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.AreasOfLaw;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.CourtFinderResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class to map legacy court finder to new fact api court finder
 */
public class CourtFinderContainer {

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

    /**
     * Get a court object from the fact api court object
     *
     * @param court : for a given court object
     * @return {@linkplain Court}
     */
    public Court getCourtFromCourtFinderResponse(uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.Court court) {
        Court courtItem = new Court();

        courtItem.setAddress(null);
        courtItem.setName(court.getName());
        courtItem.setSlug(court.getSlug());
        courtItem.setAddresses(null);
        courtItem.setAreasOfLaw(getAreaOfLawFromCourtFinderCourt(court));

        return courtItem;
    }

}
