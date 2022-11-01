package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.config.CaseStayedIncrementConfiguration;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
@Slf4j
public class TransferCaseStayedService {

    private final CoreCaseDataService coreCaseDataService;
    private final UserService userService;
    private final IdamApi idamApi;
    private final CaseStayedIncrementConfiguration caseStayedIncrementConfiguration;

    public void findCasesForTransfer() {

        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        String userId = idamApi.retrieveUserDetails(authorisation).getId();

        Integer pageNumber = caseStayedIncrementConfiguration.getPageIncrement();
        compareCases(authorisation, userId, pageNumber);

        log.info("Comparing cases to update into ccd");

        if (pageNumber <= getNumberOfPages(authorisation, userId)){
            caseStayedIncrementConfiguration.setPageIncrement(pageNumber + 1);
            if (pageNumber.equals(caseStayedIncrementConfiguration.getPageIncrement())){
                caseStayedIncrementConfiguration.setPageIncrement(1);
            }
        }
    }

    private Integer getNumberOfPages(String authorisation, String userId){
        return coreCaseDataService.getPaginationInfo(
            authorisation,
            userId,
            ClaimState.OPEN
        );
    }

    private void compareCases(String authorisation, String userId, Integer pageNumber){
        Integer numberOfPages = getNumberOfPages(authorisation, userId);

        var listOfCases = listCasesWithDeadLIne(
            authorisation,
            userId,
            pageNumber <= numberOfPages && pageNumber > 0
                ? pageNumber : 1
        );

        LocalDate currentDate = LocalDate.now();

        JSONArray listOfCasesJson = listOfCases.size() > 0
            ? new JSONArray(listOfCases) : null;

            for (int CASE_INDEX = 0 ; CASE_INDEX < listOfCases.size() ; CASE_INDEX++) {

                String intentionToProceedDeadline = listOfCasesJson
                    .getJSONObject(CASE_INDEX)
                    .get("intentionToProceedDeadline").toString();

                Long caseId = Long.parseLong(
                    listOfCasesJson
                        .getJSONObject(CASE_INDEX)
                        .get("id").toString());

               boolean currentDateAfter = currentDate
                   .isAfter(LocalDate
                       .parse(intentionToProceedDeadline));

                var ccdStayClaim = CCDCase.builder()
                    .id(caseId)
                    .build();

                if (currentDateAfter) {
                    coreCaseDataService.update(
                        authorisation,
                        ccdStayClaim,
                        CaseEvent.STAY_CLAIM
                    );
                }
            }
    }

    private List<Object> listCasesWithDeadLIne(String authorisation, String userId, Integer pageNumber) {
        var searchedCases = new ArrayList<>(coreCaseDataService.searchCases(authorisation, userId, pageNumber, ClaimState.OPEN));
        List<Object> claimsList = new ArrayList<>();
        List<Object> returnList = new ArrayList<>();
        int index = -1;
        for (var searchedCase : searchedCases) {
            Map<String, Object> searchedCaseDataMap = searchedCase.getData();
            index++;
            claimsList.add(searchedCaseDataMap);
            for (Map.Entry<String, Object> entry : searchedCaseDataMap.entrySet()) {
                if (entry.getKey().contains("intentionToProceedDeadline")){
                    returnList.add(claimsList.get(index));
                }
            }
        }
        return returnList;
    }

}
