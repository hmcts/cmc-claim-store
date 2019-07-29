package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.cmc.ccd.adapter.util.SampleData;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirection;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Address;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType.BOTH;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType.CLAIMANT;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType.DEFENDANT;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingCourtType.BIRMINGHAM;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingDurationType.FOUR_HOURS;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.EXPERT_REPORT_PERMISSION;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.OTHER;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOtherDirectionHeaderType.UPLOAD;

@RunWith(MockitoJUnitRunner.class)
public class DocAssemblyTemplateBodyMapperTest  {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Clock clock;
    @Mock
    private CourtFinderApi courtFinderApi;

    private DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    private CCDCase ccdCase;
    private UserDetails userDetails;

    private DocAssemblyTemplateBody.DocAssemblyTemplateBodyBuilder docAssemblyTemplateBodyBuilder;

    @Before
    public void setUp() {
        docAssemblyTemplateBodyMapper = new DocAssemblyTemplateBodyMapper(clock, courtFinderApi);
        ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase.setOrderGenerationData(SampleData.getCCDOrderGenerationData());
        ccdCase.setRespondents(
            ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQ())
                    .build()
            ));
        userDetails = SampleUserDetails.builder()
            .withForename("Judge")
            .withSurname("McJudge")
            .build();
        docAssemblyTemplateBodyBuilder = DocAssemblyTemplateBody.builder()
            .paperDetermination(false)
            .hasFirstOrderDirections(true)
            .hasSecondOrderDirections(true)
            .docUploadDeadline(LocalDate.parse("2020-10-11"))
            .eyewitnessUploadDeadline(LocalDate.parse("2020-10-11"))
            .currentDate(LocalDate.parse("2019-04-24"))
            .claimant(Party.builder().partyName("Individual").build())
            .defendant(Party.builder().partyName("Mary Richards").build())
            .judicial(Judicial.builder().firstName("Judge").lastName("McJudge").build())
            .referenceNumber("ref no")
            .hearingCourtName("Defendant Court")
            .extraDocUploadList(
                ImmutableList.of(
                    CCDCollectionElement.<String>builder()
                        .value("first document")
                        .build(),
                    CCDCollectionElement.<String>builder()
                        .value("second document")
                        .build()))
            .hearingCourtAddress(CCDAddress.builder()
                .addressLine1("Defendant Court address")
                .postCode("SW1P4BB")
                .postTown("London")
                .build())
            .docUploadForParty(CLAIMANT)
            .eyewitnessUploadForParty(DEFENDANT)
            .estimatedHearingDuration(FOUR_HOURS)
            .otherDirections(ImmutableList.of(
                CCDOrderDirection.builder()
                    .sendBy(LocalDate.parse("2020-10-11"))
                    .directionComment("a direction")
                    .extraOrderDirection(OTHER)
                    .otherDirectionHeaders(UPLOAD)
                    .forParty(BOTH)
                    .build(),
                CCDOrderDirection.builder()
                    .sendBy(LocalDate.parse("2020-10-11"))
                    .extraOrderDirection(EXPERT_REPORT_PERMISSION)
                    .forParty(BOTH)
                    .expertReports(
                        ImmutableList.of(
                            CCDCollectionElement.<String>builder()
                                .value("first")
                                .build(),
                            CCDCollectionElement.<String>builder()
                                .value("second")
                                .build(),
                            CCDCollectionElement.<String>builder()
                                .value("third")
                                .build()))
                    .extraDocUploadList(
                        ImmutableList.of(
                            CCDCollectionElement.<String>builder()
                                .value("first document")
                                .build(),
                            CCDCollectionElement.<String>builder()
                                .value("second document")
                                .build()))
                    .build()
            ));
        //when
        Mockito.when(clock.instant()).thenReturn(LocalDate.parse("2019-04-24")
            .atStartOfDay().toInstant(ZoneOffset.UTC));
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    public void shouldMapAddressFromCourtFinder() {
        CCDOrderGenerationData ccdOrderGenerationData = SampleData.getCCDOrderGenerationData();
        ccdOrderGenerationData.setHearingCourt(BIRMINGHAM);
        when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString())).thenReturn(ImmutableList.of(Court.builder()
            .name("Birmingham Court")
            .slug("birmingham-court")
            .address(Address.builder()
                .addressLines(ImmutableList.of("line1", "line2"))
                .postcode("SW1P4BB")
                .town("Birmingham").build()).build()));

        ccdCase.setOrderGenerationData(ccdOrderGenerationData);
        DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.from(
            ccdCase,
            userDetails);

        DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
            .paperDetermination(false)
            .hasFirstOrderDirections(true)
            .hasSecondOrderDirections(true)
            .docUploadDeadline(LocalDate.parse("2020-10-11"))
            .eyewitnessUploadDeadline(LocalDate.parse("2020-10-11"))
            .currentDate(LocalDate.parse("2019-04-24"))
            .claimant(Party.builder().partyName("Individual").build())
            .defendant(Party.builder().partyName("Mary Richards").build())
            .judicial(Judicial.builder().firstName("Judge").lastName("McJudge").build())
            .referenceNumber("ref no")
            .hearingCourtName("Birmingham Court")
            .hearingCourtAddress(CCDAddress.builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .postCode("SW1P4BB")
                .postTown("Birmingham")
                .build())
            .extraDocUploadList(
                ImmutableList.of(
                    CCDCollectionElement.<String>builder()
                        .value("first document")
                        .build(),
                    CCDCollectionElement.<String>builder()
                        .value("second document")
                        .build()))
            .docUploadForParty(CLAIMANT)
            .eyewitnessUploadForParty(DEFENDANT)
            .estimatedHearingDuration(FOUR_HOURS)
            .otherDirections(ImmutableList.of(
                CCDOrderDirection.builder()
                    .sendBy(LocalDate.parse("2020-10-11"))
                    .directionComment("a direction")
                    .otherDirectionHeaders(UPLOAD)
                    .extraOrderDirection(OTHER)
                    .forParty(BOTH)
                    .build(),
                CCDOrderDirection.builder()
                    .sendBy(LocalDate.parse("2020-10-11"))
                    .extraOrderDirection(EXPERT_REPORT_PERMISSION)
                    .forParty(BOTH)
                    .expertReports(
                        ImmutableList.of(
                            CCDCollectionElement.<String>builder()
                                .value("first")
                                .build(),
                            CCDCollectionElement.<String>builder()
                                .value("second")
                                .build(),
                            CCDCollectionElement.<String>builder()
                                .value("third")
                                .build()))
                    .extraDocUploadList(
                        ImmutableList.of(
                            CCDCollectionElement.<String>builder()
                                .value("first document")
                                .build(),
                            CCDCollectionElement.<String>builder()
                                .value("second document")
                                .build()))
                    .build()
            )).build();

        assertThat(requestBody).isEqualTo(expectedBody);
    }

    @Test
    public void shouldSerialiseTemplateBodyToExpectedJson() throws JsonProcessingException {
        String output = objectMapper.writeValueAsString(
            docAssemblyTemplateBodyMapper.from(
                ccdCase,
                userDetails));
        assertThat(output).isNotNull();
        String expected = new ResourceReader().read("/doc-assembly-template.json");
        JSONAssert.assertEquals(expected, output, STRICT);
    }

    @Test
    public void shouldMapTemplateBody() {
        DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.from(
            ccdCase,
            userDetails);

        DocAssemblyTemplateBody expectedBody = docAssemblyTemplateBodyBuilder.build();

        assertThat(requestBody).isEqualTo(expectedBody);
    }

    @Test
    public void shouldMapTemplateBodyWhenOtherDirectionIsNull() {
        CCDOrderGenerationData ccdOrderGenerationData = SampleData.getCCDOrderGenerationData();
        ccdOrderGenerationData.setOtherDirections(ImmutableList.of(
            CCDCollectionElement.<CCDOrderDirection>builder().value(null).build()));
        ccdCase.setOrderGenerationData(ccdOrderGenerationData);
        DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.from(
            ccdCase,
            userDetails);

        docAssemblyTemplateBodyBuilder.otherDirections(Collections.emptyList());
        DocAssemblyTemplateBody expectedBody = docAssemblyTemplateBodyBuilder.build();

        assertThat(requestBody).isEqualTo(expectedBody);
    }

    @Test
    public void shouldMapTemplateBodyWhenHearingCourtIsNull() {
        CCDOrderGenerationData ccdOrderGenerationData = SampleData.getCCDOrderGenerationData();
        ccdOrderGenerationData.setHearingCourt(null);
        ccdCase.setOrderGenerationData(ccdOrderGenerationData);
        DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.from(
            ccdCase,
            userDetails);

        docAssemblyTemplateBodyBuilder.hearingCourtAddress(null);
        docAssemblyTemplateBodyBuilder.hearingCourtName(null);
        DocAssemblyTemplateBody expectedBody = docAssemblyTemplateBodyBuilder.build();

        assertThat(requestBody).isEqualTo(expectedBody);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfCourtIsNotFound() {
        CCDOrderGenerationData ccdOrderGenerationData = SampleData.getCCDOrderGenerationData();
        ccdOrderGenerationData.setHearingCourt(BIRMINGHAM);
        ccdCase.setOrderGenerationData(ccdOrderGenerationData);
        when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString())).thenReturn(Collections.emptyList());
        docAssemblyTemplateBodyMapper.from(
            ccdCase,
            userDetails);
    }

}
