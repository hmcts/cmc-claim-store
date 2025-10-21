package uk.gov.hmcts.cmc.claimstore.services.courtfinder;

import feign.FeignException;
import feign.codec.DecodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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

    private static final Logger LOG = LoggerFactory.getLogger(CourtFinderService.class);

    private final CourtFinderApi courtFinderApi;

    public static final String MONEY_CLAIM_AOL = "Money claims";

    public CourtFinderService(CourtFinderApi courtFinderApi) {
        this.courtFinderApi = courtFinderApi;
    }

    public List<Court> getCourtDetailsListFromPostcode(String postcode) {
        SearchCourtByPostcodeResponse searchByPostcodeResponse;
        try {
            searchByPostcodeResponse = courtFinderApi.findMoneyClaimCourtByPostcode(postcode);
        } catch (DecodeException ex) {
            LOG.warn("Failed to decode court finder response for postcode {}: {}", postcode, ex.getMessage());
            LOG.debug("Court finder decode failure for postcode {}", postcode, ex);
            return new ArrayList<>();
        } catch (FeignException | RestClientException ex) {
            LOG.warn("Failed to retrieve court details for postcode {}: {}", postcode, ex.getMessage());
            LOG.debug("Court finder client failure for postcode {}", postcode, ex);
            return new ArrayList<>();
        } catch (RuntimeException ex) {
            LOG.warn("Unexpected error retrieving court details for postcode {}: {}", postcode, ex.getMessage());
            LOG.debug("Unexpected court finder failure for postcode {}", postcode, ex);
            return new ArrayList<>();
        }

        List<Court> courtList = new ArrayList<>();
        if (ofNullable(searchByPostcodeResponse).isEmpty()
            || ofNullable(searchByPostcodeResponse.getCourts()).isEmpty()) {
            return courtList;
        }
        for (CourtDetails courtDetails : searchByPostcodeResponse.getCourts()) {
            courtList.add(getCourtDetailsSafely(courtDetails.getSlug()));
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

    private Court getCourtDetailsSafely(String slug) {
        try {
            return getCourtDetailsFromSlug(slug);
        } catch (FeignException | RestClientException ex) {
            LOG.warn("Failed to retrieve court details for slug {}", slug, ex);
            return Court.builder().slug(slug).build();
        } catch (RuntimeException ex) {
            LOG.warn("Unexpected error retrieving court details for slug {}", slug, ex);
            return Court.builder().slug(slug).build();
        }
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
