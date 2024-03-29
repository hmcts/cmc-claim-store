package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
@Slf4j
public class TransferCaseStayedService {

    private final CoreCaseDataService coreCaseDataService;
    private final UserService userService;
    private final IdamApi idamApi;

    private static final int MAX_ALLOWED_PAGE_PROCESS = 1000;

    public void findCasesForTransfer() {

        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        String userId = idamApi.retrieveUserDetails(authorisation).getId();

        Integer numberOfPages = getNumberOfPages(authorisation, userId);
        int pageNumber = 1;

        log.info("Comparing cases to update into ccd");

        do {
            compareCases(authorisation, userId, pageNumber);
            pageNumber++;
            if (pageNumber == (MAX_ALLOWED_PAGE_PROCESS + 1)) { 
                break;
            }
        } while (pageNumber <= numberOfPages);
    }

    public void compareCases(String authorisation, String userId, Integer pageNumber) {
        Integer numberOfPages = getNumberOfPages(authorisation, userId);

        var listOfCases = listCasesWithDeadLIne(
            authorisation,
            userId,
            pageNumber <= numberOfPages && pageNumber > 0
                ? pageNumber : 1
        );

        LocalDate currentDate = LocalDate.now();

        JSONArray listOfCasesJson = !listOfCases.isEmpty()
            ? new JSONArray(listOfCases) : null;

        for (int caseIndex = 0; caseIndex < listOfCases.size(); caseIndex++) {
            String intentionToProceedDeadline = null;
            Long caseId = null;

            if (listOfCasesJson.get(caseIndex) != null
                && !listOfCasesJson.isEmpty()
                && listOfCasesJson != null
            ) {

                intentionToProceedDeadline = listOfCasesJson
                    .getJSONObject(caseIndex)
                    .get("intentionToProceedDeadline").toString();

                caseId = Long.parseLong(
                    listOfCasesJson
                        .getJSONObject(caseIndex)
                        .get("id").toString());
            }

            boolean currentDateAfter = intentionToProceedDeadline != null && currentDate
                .isAfter(LocalDate
                    .parse(intentionToProceedDeadline));

            CCDCase ccdStayClaim = caseId != null ? CCDCase.builder()
                .id(caseId)
                .build() : null;

            if (currentDateAfter && ccdStayClaim != null) {
                coreCaseDataService.caseTransferUpdate(
                    authorisation,
                    ccdStayClaim,
                    CaseEvent.STAY_CLAIM
                );
            }
        }
    }

    private Integer getNumberOfPages(String authorisation, String userId) {
        return coreCaseDataService.getPaginationInfo(
            authorisation,
            userId,
            getSearchCriteria(false, null)
        );
    }

    private Map<String, String> getSearchCriteria(boolean isSearchingPerPageEnabled, Integer pageNumber) {
        Map<String, String> searchCriteria = new HashMap<>();

        if (isSearchingPerPageEnabled && pageNumber != null) {
            searchCriteria.put("page", pageNumber.toString());
        }

        searchCriteria.put("sortDirection", "asc");
        searchCriteria.put("state", ClaimState.OPEN.getValue());

        return searchCriteria;
    }

    private List<Object> listCasesWithDeadLIne(String authorisation, String userId, Integer pageNumber) {
        var searchedCases = new ArrayList<>(coreCaseDataService.searchCases(
            authorisation,
            userId,
            getSearchCriteria(
                true,
                pageNumber
            )
        ));

        List<Object> claimsList = new ArrayList<>();
        List<Object> generatedList = new ArrayList<>();
        int index = -1;

        for (var searchedCase : searchedCases) {
            Map<String, Object> searchedCaseDataMap = searchedCase.getData();
            index++;
            claimsList.add(searchedCaseDataMap);
            for (Map.Entry<String, Object> entry : searchedCaseDataMap.entrySet()) {
                if (entry.getKey().contains("intentionToProceedDeadline")) {
                    generatedList.add(claimsList.get(index));
                }
            }
        }
        return generatedList;
    }
}
