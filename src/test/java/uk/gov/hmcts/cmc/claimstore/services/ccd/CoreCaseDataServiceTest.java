package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDSettlement;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDResponse;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.CountyCourtJudgmentMapper;
import uk.gov.hmcts.cmc.ccd.mapper.offers.SettlementMapper;
import uk.gov.hmcts.cmc.ccd.mapper.response.ResponseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.ReferenceNumberService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoreCaseDataServiceTest {

    private static final String USER_AUTHORISATION = "Bearer UserAuthorisationToken";

    @Mock
    SaveCoreCaseDataService saveCoreCaseDataService;
    @Mock
    UpdateCoreCaseDataService updateCoreCaseDataService;
    @Mock
    CaseMapper caseMapper;
    @Mock
    CountyCourtJudgmentMapper countyCourtJudgmentMapper;
    @Mock
    ResponseMapper responseMapper;
    @Mock
    SettlementMapper settlementMapper;
    @Mock
    UserService userService;
    @Mock
    JsonMapper jsonMapper;
    @Mock
    ReferenceNumberService referenceNumberService;

    private CoreCaseDataService service;

    private static final UserDetails authenticatedUser = new UserDetails("1", "a@example.com", "A", "B", new ArrayList<>());
    private static CCDCase ccdCase = CCDCase.builder().id(2L).build();
    private static Claim claim = SampleClaim.getDefault();

    @Before
    public void setup() {
        when(userService.getUserDetails(USER_AUTHORISATION)).thenReturn(authenticatedUser);
        when(caseMapper.to(eq(claim))).thenReturn(ccdCase);

        service = new CoreCaseDataService(
            saveCoreCaseDataService,
            updateCoreCaseDataService,
            caseMapper,
            countyCourtJudgmentMapper,
            responseMapper,
            settlementMapper,
            userService,
            jsonMapper,
            referenceNumberService
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void saveThrowCoreCaseDataStoreException() {
        when(caseMapper.to(any())).thenThrow(new RuntimeException("Any exception should be mapped to expected one"));

        service.save(USER_AUTHORISATION, mock(Claim.class));
    }

    @Test
    public void saveShouldReturnValidClaim() {
        Claim expectedClaim = SampleClaim.getDefault();
        CCDCase tmpCcdCaseObj = CCDCase.builder().build();
        boolean isRepresented = claim.getClaimData().isClaimantRepresented();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1L)
            .data(new HashMap<>())
            .build();

        when(referenceNumberService.getReferenceNumber(eq(isRepresented))).thenReturn("reference");
        when(saveCoreCaseDataService.save(
            eq(USER_AUTHORISATION),
            any(EventRequestData.class),
            eq(ccdCase),
            eq(isRepresented),
            eq(claim.getLetterHolderId())
            )
        ).thenReturn(caseDetails);

        when(jsonMapper.convertValue(any(Map.class), any())).thenReturn(tmpCcdCaseObj);
        when(caseMapper.from(eq(tmpCcdCaseObj))).thenReturn(expectedClaim);

        Claim actual = service.save(USER_AUTHORISATION, claim);

        assertThat(actual).isEqualTo(expectedClaim);
    }

    @Test
    public void requestMoreTimeForResponseShouldCallUpdateCoreService() {
        service.requestMoreTimeForResponse(USER_AUTHORISATION, claim, LocalDate.now().plusDays(10));

        verify(updateCoreCaseDataService).update(
            eq(USER_AUTHORISATION),
            any(EventRequestData.class),
            eq(ccdCase),
            eq(ccdCase.getId())
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void requestMoreTimeForResponseThrowCoreCaseDataStoreException() {
        when(updateCoreCaseDataService.update(any(), any(), any(), any())).thenThrow(new RuntimeException("Any"));

        service.requestMoreTimeForResponse(USER_AUTHORISATION, claim, LocalDate.now().plusDays(10));
    }

    @Test
    public void saveCountyCourtJudgmentShouldCallUpdateCoreService() {
        CountyCourtJudgment ccj = mock(CountyCourtJudgment.class);
        CCDCountyCourtJudgment ccdCcj = CCDCountyCourtJudgment.builder().build();

        when(countyCourtJudgmentMapper.to(eq(ccj))).thenReturn(ccdCcj);

        service.saveCountyCourtJudgment(USER_AUTHORISATION, claim, ccj);

        verify(updateCoreCaseDataService).update(
            eq(USER_AUTHORISATION),
            any(EventRequestData.class),
            eq(ccdCase),
            eq(ccdCase.getId())
        );
    }

    @Test
    public void saveDefendantResponseShouldCallUpdateCoreService() {
        FullDefenceResponse response = mock(FullDefenceResponse.class);

        when(responseMapper.to(eq(response))).thenReturn(CCDResponse.builder().build());

        service.saveDefendantResponse(claim, "email@example.com", response, USER_AUTHORISATION);

        verify(updateCoreCaseDataService).update(
            eq(USER_AUTHORISATION),
            any(EventRequestData.class),
            eq(ccdCase),
            eq(ccdCase.getId())
        );
    }

    @Test
    public void saveSettlementShouldCallUpdateCoreService() {
        Settlement settlement = mock(Settlement.class);
        Long ccdId = 1L;

        when(settlementMapper.to(eq(settlement))).thenReturn(CCDSettlement.builder().build());

        service.saveSettlement(ccdId, settlement, USER_AUTHORISATION, CaseEvent.SETTLED_PRE_JUDGMENT);

        verify(updateCoreCaseDataService).update(
            eq(USER_AUTHORISATION),
            any(EventRequestData.class),
            any(CCDCase.class),
            eq(ccdId)
        );
    }

    @Test
    public void reachSettlementAgreementShouldCallUpdateCoreService() {
        Settlement settlement = mock(Settlement.class);
        Long ccdId = 1L;

        when(settlementMapper.to(eq(settlement))).thenReturn(CCDSettlement.builder().build());

        service.reachSettlementAgreement(ccdId, settlement, USER_AUTHORISATION, CaseEvent.SETTLED_PRE_JUDGMENT);

        verify(updateCoreCaseDataService).update(
            eq(USER_AUTHORISATION),
            any(EventRequestData.class),
            any(CCDCase.class),
            eq(ccdId)
        );
    }

    @Test
    public void updateResponseDeadlineShouldCallUpdateCoreService() {
        service.updateResponseDeadline(USER_AUTHORISATION, claim, LocalDate.now());

        verify(updateCoreCaseDataService).update(
            eq(USER_AUTHORISATION),
            any(EventRequestData.class),
            any(CCDCase.class),
            eq(claim.getId())
        );
    }
}
