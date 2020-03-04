package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirection;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.DirectionOrderService;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType.BOTH;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType.CLAIMANT;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType.DEFENDANT;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingDurationType.FOUR_HOURS;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.EXPERT_REPORT_PERMISSION;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.OTHER;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOtherDirectionHeaderType.UPLOAD;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@ExtendWith(MockitoExtension.class)
class DocAssemblyTemplateBodyMapperTest {

    private static final String SUBMIT_MORE_DOCS_INSTRUCTION = "submit more docs";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Clock clock;
    @Mock
    private DirectionOrderService directionOrderService;
    @Mock
    private WorkingDayIndicator workingDayIndicator;

    private DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    private CCDCase ccdCase;
    private UserDetails userDetails;
    private DocAssemblyTemplateBody.DocAssemblyTemplateBodyBuilder docAssemblyTemplateBodyBuilder;

    @BeforeEach
    void setUp() {
        docAssemblyTemplateBodyMapper
            = new DocAssemblyTemplateBodyMapper(clock, directionOrderService, workingDayIndicator);

        ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);
        ccdCase.setHearingCourt("BIRMINGHAM");
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
            .extraDocUploadList(
                ImmutableList.of(
                    CCDCollectionElement.<String>builder()
                        .value("first document")
                        .build(),
                    CCDCollectionElement.<String>builder()
                        .value("second document")
                        .build()))
            .hearingCourtName("Birmingham Court")
            .hearingCourtAddress(CCDAddress.builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .addressLine3("line3")
                .postCode("SW1P4BB")
                .postTown("Birmingham")
                .build())
            .docUploadForParty(CLAIMANT)
            .eyewitnessUploadForParty(DEFENDANT)
            .estimatedHearingDuration(FOUR_HOURS)
            .otherDirections(ImmutableList.of(
                OtherDirection.builder()
                    .sendBy(LocalDate.parse("2020-10-11"))
                    .directionComment("a direction")
                    .extraOrderDirection(OTHER)
                    .otherDirectionHeaders(UPLOAD)
                    .forParty(BOTH)
                    .build(),
                OtherDirection.builder()
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
            ))
            .expertReportPermissionPartyAskedByClaimant(true)
            .expertReportPermissionPartyAskedByDefendant(true)
            .expertReportInstructionClaimant(Collections.emptyList())
            .expertReportInstructionDefendant(Collections.emptyList())
            .grantExpertReportPermission(true)
            .expertReportInstruction(SUBMIT_MORE_DOCS_INSTRUCTION);

        //when
        when(clock.instant()).thenReturn(LocalDate.parse("2019-04-24")
            .atStartOfDay().toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.withZone(UTC_ZONE)).thenReturn(clock);
    }

    @Nested
    @DisplayName("Hearing court")
    class HearingCourtTests {
        private DocAssemblyTemplateBody expectedBody;

        @BeforeEach
        void setUp() {
            expectedBody = DocAssemblyTemplateBody.builder()
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
                    .addressLine3("line3")
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
                    OtherDirection.builder()
                        .sendBy(LocalDate.parse("2020-10-11"))
                        .directionComment("a direction")
                        .otherDirectionHeaders(UPLOAD)
                        .extraOrderDirection(OTHER)
                        .forParty(BOTH)
                        .build(),
                    OtherDirection.builder()
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
                ))
                .directionDeadline(workingDayIndicator.getNextWorkingDay(
                    LocalDate.parse("2019-04-24").plusDays(19)))
                .changeOrderDeadline(workingDayIndicator.getNextWorkingDay(
                    LocalDate.parse("2019-04-24").plusDays(12)))
                .expertReportPermissionPartyAskedByClaimant(true)
                .expertReportPermissionPartyAskedByDefendant(true)
                .expertReportInstructionClaimant(Collections.emptyList())
                .expertReportInstructionDefendant(Collections.emptyList())
                .grantExpertReportPermission(true)
                .expertReportInstruction(SUBMIT_MORE_DOCS_INSTRUCTION)
                .build();
        }
    }

    @Nested
    @DisplayName("General Tests")
    class GeneralTests {
        @BeforeEach
        void setUp() {
            when(directionOrderService.getHearingCourt(any()))
                .thenReturn(HearingCourt.builder()
                    .name("Birmingham Court")
                    .address(CCDAddress.builder()
                        .addressLine1("line1")
                        .addressLine2("line2")
                        .addressLine3("line3")
                        .postCode("SW1P4BB")
                        .postTown("Birmingham")
                        .build()
                    )
                    .build());
        }

        @Test
        void shouldSerialiseTemplateBodyToExpectedJson() throws JsonProcessingException {
            String output = objectMapper.writeValueAsString(
                docAssemblyTemplateBodyMapper.from(
                    ccdCase,
                    userDetails
                )
            );
            assertThat(output).isNotNull();
            String expected = new ResourceReader().read("/doc-assembly-template.json");
            JSONAssert.assertEquals(expected, output, STRICT);
            verify(directionOrderService).getHearingCourt(any());
        }

        @Test
        void shouldMapTemplateBody() {
            DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.from(
                ccdCase,
                userDetails
            );

            DocAssemblyTemplateBody expectedBody = docAssemblyTemplateBodyBuilder.build();

            assertThat(requestBody).isEqualTo(expectedBody);
            verify(directionOrderService).getHearingCourt(any());
        }

        @Test
        void shouldMapTemplateBodyWhenOtherDirectionIsNull() {
            ccdCase = SampleData.addCCDOrderGenerationData(ccdCase).toBuilder()
                .otherDirections(ImmutableList.of(CCDCollectionElement
                    .<CCDOrderDirection>builder()
                    .value(null)
                    .build()))
                .build();

            DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.from(
                ccdCase,
                userDetails
            );

            docAssemblyTemplateBodyBuilder.otherDirections(Collections.emptyList());
            DocAssemblyTemplateBody expectedBody = docAssemblyTemplateBodyBuilder.build();

            assertThat(requestBody).isEqualTo(expectedBody);
            verify(directionOrderService).getHearingCourt(any());
        }
    }
}
