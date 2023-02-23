package uk.gov.hmcts.cmc.claimstore.services.courtfinder;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Court;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.name.SearchCourtByNameResponse;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.postcode.CourtDetails;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.postcode.SearchCourtByPostcodeResponse;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.slug.SearchCourtBySlugResponse;
import uk.gov.hmcts.cmc.claimstore.requests.courtfinder.CourtFinderApi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
public class CourtFinderService {

    private final CourtFinderApi courtFinderApi;

    public static final String MONEY_CLAIM_AOL = "Money claims";

    public CourtFinderService(CourtFinderApi courtFinderApi) {
        this.courtFinderApi = courtFinderApi;
    }

    public List<Court> getCourtDetailsListFromPostcode(String postcode) {
        SearchCourtByPostcodeResponse searchByPostcodeResponse = courtFinderApi.findMoneyClaimCourtByPostcode(postcode);

        List<Court> courtList = new ArrayList<>();
        if (ofNullable(searchByPostcodeResponse).isEmpty()) {
            return courtList;
        }
        for (CourtDetails courtDetails : searchByPostcodeResponse.getCourts()) {
            Court court = getCourtDetailsFromSlug(courtDetails.getSlug());
            courtList.add(court);
        }
        return courtList;
    }

    public Court getCourtDetailsFromSlug(String slug) {
        SearchCourtBySlugResponse searchCourtBySlugResponse = courtFinderApi.getCourtDetailsFromNameSlug(slug);

        return Court.builder()
            .name(searchCourtBySlugResponse.getName())
            .slug(searchCourtBySlugResponse.getSlug())
            .addresses(searchCourtBySlugResponse.getAddresses())
            .facilities(searchCourtBySlugResponse.getFacilities())
            .build();
    }

    public List<Court> getCourtsByName(String name) {
        List<SearchCourtByNameResponse> searchCourtByNameResponses = courtFinderApi.findMoneyClaimCourtByName(name);
        List<Court> courtList = new ArrayList<>();

        for (SearchCourtByNameResponse searchCourtByNameResponse : searchCourtByNameResponses) {
            SearchCourtBySlugResponse searchCourtBySlugResponse = courtFinderApi.getCourtDetailsFromNameSlug(searchCourtByNameResponse.getSlug());

            Court court = Court.builder()
                .name(searchCourtByNameResponse.getName())
                .slug(searchCourtByNameResponse.getSlug())
                .areasOfLaw(searchCourtBySlugResponse.getAreasOfLaw())
                .addresses(searchCourtBySlugResponse.getAddresses())
                .build();
            courtList.add(court);
        }

        return courtList
            .stream()
            .filter(
                c -> c.getAreasOfLaw()
                    .stream()
                    .anyMatch(a -> a.getName().equalsIgnoreCase(MONEY_CLAIM_AOL))
            ).collect(Collectors.toList());
    }
}
