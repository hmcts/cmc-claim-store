package uk.gov.hmcts.cmc.claimstore.services.legaladvisor;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirection;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingTimeType.FOUR_HOURS;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.OTHER;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDPartyForDirectionType.BOTH;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDPartyForDirectionType.CLAIMANT;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDPartyForDirectionType.DEFENDANT;

@RunWith(MockitoJUnitRunner.class)
public class DocAssemblyClientTest {

    private static final String BEARER_TOKEN = "Bearer coffee";
    private static final String SERVICE_TOKEN = "Bearer morecoffee";

    @Mock
    private DocAssemblyApi docAssemblyApi;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UserService userService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private JsonMapper jsonMapper;

    private DocAssemblyClient docAssemblyClient;

    @Before
    public void setUp() {
        docAssemblyClient = new DocAssemblyClient(
            docAssemblyApi,
            userService,
            authTokenGenerator,
            jsonMapper
        );
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        when(userService.getUserDetails(BEARER_TOKEN).getSurname()).thenReturn(Optional.of("Dredd"));
        when(userService.getUserDetails(BEARER_TOKEN).getForename()).thenReturn("Judge");
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(jsonMapper.fromMap(Collections.emptyMap(), CCDCase.class)).thenReturn(ccdCase);
    }

    @Test
    public void shouldGetAResponse() {

        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId("Q1YtQ01DLUdPUi1FTkctMDAwNC5kb2N4")
            .formPayload(
                DocAssemblyRequest.FormPayload.builder()
                    .claimant(DocAssemblyRequest.Party.builder()
                        .partyName("Individual").build())
                    .defendant(DocAssemblyRequest.Party.builder()
                        .partyName("Mary Richards").build())
                    .currentDate(LocalDate.now())
                    .docUploadDeadline(LocalDate.parse("2020-10-11"))
                    .docUploadForParty(CLAIMANT)
                    .eyewitnessUploadDeadline(LocalDate.parse("2020-10-11"))
                    .eyewitnessUploadForParty(DEFENDANT)
                    .mediationForParty(BOTH)
                    .estimatedHearingDuration(FOUR_HOURS)
                    .hasFirstOrderDirections(true)
                    .hasSecondOrderDirections(true)
                    .hasThirdOrderDirections(false)
                    .hearingIsRequired(true)
                    .judicial(DocAssemblyRequest.Judicial.builder()
                        .firstName("Judge")
                        .lastName("Dredd")
                        .build())
                    .preferredCourtName("Some court")
                    .preferredCourtAddress("this is an address EC2 BLA")
                    .hearingStatement("No idea")
                    .referenceNumber("ref no")
                    .otherDirectionList(ImmutableList.of(
                        CCDOrderDirection.builder()
                            .extraOrderDirection(OTHER)
                            .otherDirection("a direction")
                            .forParty(BOTH)
                            .build()))
                    .build()
            )
            .build();
        DocAssemblyResponse docAssemblyResponse = new DocAssemblyResponse();
        when(docAssemblyApi.generateOrder(
            docAssemblyRequest,
            BEARER_TOKEN,
            SERVICE_TOKEN))
            .thenReturn(docAssemblyResponse);

        DocAssemblyResponse response =
            docAssemblyClient.generateOrder(BEARER_TOKEN, Collections.emptyMap());

        assertThat(response).isEqualTo(docAssemblyResponse);
    }

    @Test(expected = DocumentGenerationFailedException.class)
    public void shouldThrowIfCallToDocAssemblyFails() {
        when(docAssemblyApi.generateOrder(
            any(DocAssemblyRequest.class),
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN))).thenThrow(RuntimeException.class);
        docAssemblyClient.generateOrder(BEARER_TOKEN, Collections.emptyMap());
    }

}
