package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.ccd.domain.GeneralLetterContent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseMethod;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDBespokeOrderWarning;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirection;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.DirectionOrderService;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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
    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;

    @Captor
    ArgumentCaptor<LocalDate> workingDayIndicate;

    private DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    private CCDCase ccdCase;
    private UserDetails userDetails;
    private DocAssemblyTemplateBody.DocAssemblyTemplateBodyBuilder docAssemblyTemplateBodyBuilder;
    private static final String LETTER_CONTENT = "letter content";
    private GeneralLetterContent.GeneralLetterContentBuilder letterContent;

    @BeforeEach
    void setUp() {
        docAssemblyTemplateBodyMapper
            = new DocAssemblyTemplateBodyMapper(clock,
            directionOrderService,
            workingDayIndicator,
            responseDeadlineCalculator);
        ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);
        ccdCase.setHearingCourt("BIRMINGHAM");
        ccdCase.setCaseName("case name");
        ccdCase.setRespondents(
            ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQ())
                    .build()
            ));
        ccdCase.setApplicants(
            ImmutableList.of(
                CCDCollectionElement.<CCDApplicant>builder()
                    .value(SampleData.getCCDApplicantIndividual())
                    .build()
            ));
        letterContent = GeneralLetterContent.builder()
            .letterContent(LETTER_CONTENT)
            .caseworkerName("Judge McJudge");

        userDetails = SampleUserDetails.builder()
            .withForename("Judge")
            .withSurname("McJudge")
            .build();

        docAssemblyTemplateBodyBuilder = DocAssemblyTemplateBody.builder()
            .paperDetermination(false)
            .hasFirstOrderDirections(true)
            .hasSecondOrderDirections(true)
            .docUploadDeadline(LocalDate.parse("2022-10-11"))
            .eyewitnessUploadDeadline(LocalDate.parse("2022-10-11"))
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
                    .sendBy(LocalDate.parse("2022-10-11"))
                    .directionComment("a direction")
                    .extraOrderDirection(OTHER)
                    .otherDirectionHeaders(UPLOAD)
                    .forParty(BOTH)
                    .build(),
                OtherDirection.builder()
                    .sendBy(LocalDate.parse("2022-10-11"))
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
            .isOconRespone("false")
            .expertReportInstruction(SUBMIT_MORE_DOCS_INSTRUCTION);
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
                .docUploadDeadline(LocalDate.parse("2022-10-11"))
                .eyewitnessUploadDeadline(LocalDate.parse("2022-10-11"))
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

            //when
            when(clock.instant()).thenReturn(LocalDate.parse("2019-04-24")
                .atStartOfDay().toInstant(ZoneOffset.UTC));
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);
            when(clock.withZone(UTC_ZONE)).thenReturn(clock);
        }
    }

    @Nested
    @DisplayName("General Tests for General Order")
    class GeneralTestsForGeneralOrder {
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

            //when
            when(clock.instant()).thenReturn(LocalDate.parse("2019-04-24")
                .atStartOfDay().toInstant(ZoneOffset.UTC));
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);
            when(clock.withZone(UTC_ZONE)).thenReturn(clock);
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

        @Test
        void shouldMapTemplateBodyWithOconResponse() {
            ReflectionTestUtils.setField(docAssemblyTemplateBodyMapper, "reconsiderationDaysForOconResponse", 12);
            ReflectionTestUtils.setField(docAssemblyTemplateBodyMapper, "reconsiderationDaysForOnlineResponse", 7);
            LocalDate now = LocalDate.now();
            CCDRespondent ccdRespondentIndividual = SampleData.getCCDRespondentIndividual()
                .toBuilder()
                .responseDeadline(now)
                .responseMethod(CCDResponseMethod.OCON_FORM)
                .build();
            ccdCase.setRespondents(
                ImmutableList.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(ccdRespondentIndividual)
                        .build()
                ));

            docAssemblyTemplateBodyBuilder.isOconRespone("true");
            DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.from(
                ccdCase,
                userDetails
            );
            DocAssemblyTemplateBody expectedBody = docAssemblyTemplateBodyBuilder.build();
            assertThat(requestBody).isEqualTo(expectedBody);
            verify(workingDayIndicator, times(3)).getNextWorkingDay(workingDayIndicate.capture());
            assertEquals(LocalDate.parse("2019-05-01"), workingDayIndicate.getAllValues().get(0));
            assertEquals(LocalDate.parse("2019-05-06"), workingDayIndicate.getAllValues().get(1));

        }

        @Test
        void shouldMapTemplateBodyWithOnlineResponse() {
            LocalDate now = LocalDate.now();
            CCDRespondent ccdRespondentIndividual = SampleData.getCCDRespondentIndividual()
                .toBuilder()
                .responseDeadline(now)
                .responseMethod(CCDResponseMethod.DIGITAL)
                .build();
            ccdCase.setRespondents(
                ImmutableList.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(ccdRespondentIndividual)
                        .build()
                ));
            DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.from(
                ccdCase,
                userDetails
            );

            docAssemblyTemplateBodyBuilder.isOconRespone("false");
            DocAssemblyTemplateBody expectedBody = docAssemblyTemplateBodyBuilder.build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }

        /*@Test
        void shouldMapDirectionDeadLineBefore() {
            ReflectionTestUtils.setField(docAssemblyTemplateBodyMapper, "reconsiderationDaysForOconResponse", 12);
            ReflectionTestUtils.setField(docAssemblyTemplateBodyMapper, "reconsiderationDaysForOnlineResponse", 7);
            ReflectionTestUtils.setField(docAssemblyTemplateBodyMapper, "directionDeadlineChangeDate",
                "2018-06-27T11:00:00");
            when(workingDayIndicator.getNextWorkingDay(any())).thenReturn(LocalDate.parse("2020-07-28"));
            DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.from(
                ccdCase,
                userDetails
            );
            verify(workingDayIndicator, times(2)).getNextWorkingDay(workingDayIndicate.capture());
            assertEquals(LocalDate.parse("2019-05-01"), workingDayIndicate.getAllValues().get(0));

        }

        @Test
        void shouldMapDirectionDeadLineAfter() {

            ReflectionTestUtils.setField(docAssemblyTemplateBodyMapper, "directionDeadLineNumberOfDays", 7);
            ReflectionTestUtils.setField(docAssemblyTemplateBodyMapper, "directionDeadlineChangeDate",
                LocalDateTime.now().plusDays(2).toString());
            when(workingDayIndicator.getNextWorkingDay(any())).thenReturn(LocalDate.parse("2020-07-28"));
            DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.from(
                ccdCase,
                userDetails
            );
            verify(workingDayIndicator, times(2)).getNextWorkingDay(workingDayIndicate.capture());
            assertEquals(LocalDate.parse("2019-05-13"), workingDayIndicate.getAllValues().get(0));

        }*/
    }

    @Nested
    @DisplayName("General Tests for General Letter")
    class GeneralTestsForGeneralLetter {

        @BeforeEach
        void setUp() {
            //when
            when(clock.instant()).thenReturn(LocalDate.parse("2019-04-24")
                .atStartOfDay().toInstant(ZoneOffset.UTC));
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);
            when(clock.withZone(UTC_ZONE)).thenReturn(clock);
        }

        @Test
        void shouldMapTemplateBodyWhenGeneralLetterForDefendant() {
            letterContent.issueLetterContact(CCDContactPartyType.DEFENDANT);
            ccdCase.setGeneralLetterContent(letterContent.build());
            DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.generalLetterBody(ccdCase);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .currentDate(LocalDate.parse("2019-04-24"))
                .partyName("Mary Richards")
                .partyAddress(CCDAddress.builder()
                    .addressLine1("line1")
                    .addressLine2("line2")
                    .addressLine3("line3")
                    .postCode("postcode")
                    .postTown("city")
                    .build())
                .referenceNumber("ref no")
                .caseName("case name")
                .caseworkerName("Judge McJudge")
                .body(LETTER_CONTENT)
                .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }

        @Test
        void shouldMapTemplateBodyWhenGeneralLetterForClaimant() {
            letterContent.issueLetterContact(CCDContactPartyType.CLAIMANT);
            ccdCase.setGeneralLetterContent(letterContent.build());
            DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.generalLetterBody(ccdCase);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .currentDate(LocalDate.parse("2019-04-24"))
                .partyName("Individual")
                .partyAddress(CCDAddress.builder()
                    .addressLine1("line1")
                    .addressLine2("line2")
                    .addressLine3("line3")
                    .postCode("postcode")
                    .postTown("city")
                    .build())
                .referenceNumber("ref no")
                .caseName("case name")
                .caseworkerName("Judge McJudge")
                .body(LETTER_CONTENT)
                .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }
    }

    @Nested
    class PaperDefenceFormTests {
        private String hearingCourt;
        private LocalDate deadline;
        private LocalDate extendedDeadline;
        private String totalAmount;

        @BeforeEach
        void setUp() {
            deadline = LocalDate.now();
            totalAmount = "100";
            hearingCourt = "hearing court";
            ccdCase.setHearingCourtName(hearingCourt);
            ccdCase.setCalculatedResponseDeadline(deadline);
            ccdCase.setTotalAmount(totalAmount);

            extendedDeadline = deadline.plusDays(14);
            when(responseDeadlineCalculator.calculatePostponedResponseDeadline(ccdCase.getIssuedOn()))
                .thenReturn(extendedDeadline);
        }

        @Test
        void shouldMapTemplateBodyIndividualYesToNewFeature() {
            DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.paperDefenceForm(ccdCase);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .referenceNumber("ref no")
                .responseDeadline(ccdCase.getRespondents().get(0).getValue().getResponseDeadline())
                .extendedResponseDeadline(extendedDeadline)
                .partyName("Individual")
                .partyAddress(CCDAddress.builder()
                    .addressLine1("line1")
                    .addressLine2("line2")
                    .addressLine3("line3")
                    .postCode("postcode")
                    .postTown("city")
                    .build()
                )
                .totalAmount(totalAmount)
                .claimantName("Individual")
                .claimantAddress(CCDAddress.builder()
                    .addressLine1("line1")
                    .addressLine2("line2")
                    .addressLine3("line3")
                    .postCode("postcode")
                    .postTown("city")
                    .build())
                .claimantEmail("my@email.com")
                .hearingCourtName(hearingCourt)
                .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }

        @Test
        void shouldMapTemplateBodySoleTraderYesToNewFeature() {
            LocalDate now = LocalDate.now();
            CCDRespondent ccdRespondentSoleTrader = SampleData.getCCDRespondentSoleTrader()
                .toBuilder()
                .responseDeadline(now)
                .build();
            ccdCase.setRespondents(
                ImmutableList.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(ccdRespondentSoleTrader)
                        .build()
                ));

            DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.paperDefenceForm(ccdCase);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .referenceNumber("ref no")
                .responseDeadline(now)
                .extendedResponseDeadline(extendedDeadline)
                .partyName("SoleTrader")
                .partyAddress(CCDAddress.builder()
                    .addressLine1("line1")
                    .addressLine2("line2")
                    .addressLine3("line3")
                    .postCode("postcode")
                    .postTown("city")
                    .build()
                )
                .totalAmount(totalAmount)
                .claimantName("Individual")
                .claimantAddress(CCDAddress.builder()
                    .addressLine1("line1")
                    .addressLine2("line2")
                    .addressLine3("line3")
                    .postCode("postcode")
                    .postTown("city")
                    .build())
                .claimantEmail("my@email.com")
                .hearingCourtName(hearingCourt)
                .businessName("My Trade")
                .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }

        @Test
        void shouldMapTemplateBodyCompanyYesToNewFeature() {
            LocalDate now = LocalDate.now();
            CCDRespondent ccdRespondentCompany = SampleData.getCCDRespondentCompany()
                .toBuilder()
                .responseDeadline(now)
                .build();
            ccdCase.setRespondents(
                ImmutableList.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(ccdRespondentCompany)
                        .build()
                ));

            DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.paperDefenceForm(ccdCase);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .referenceNumber("ref no")
                .responseDeadline(now)
                .extendedResponseDeadline(extendedDeadline)
                .partyName("Abc Ltd")
                .partyAddress(CCDAddress.builder()
                    .addressLine1("line1")
                    .addressLine2("line2")
                    .addressLine3("line3")
                    .postCode("postcode")
                    .postTown("city")
                    .build()
                )
                .totalAmount(totalAmount)
                .claimantName("Individual")
                .claimantAddress(CCDAddress.builder()
                    .addressLine1("line1")
                    .addressLine2("line2")
                    .addressLine3("line3")
                    .postCode("postcode")
                    .postTown("city")
                    .build())
                .claimantEmail("my@email.com")
                .hearingCourtName(hearingCourt)
                .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }
    }

    @Test
    void shouldMapTemplateBodyWhenPaperResponseAdmissionLetter() {
        when(clock.instant()).thenReturn(LocalDate.parse("2020-06-22").atStartOfDay().toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.withZone(LocalDateTimeFactory.UTC_ZONE)).thenReturn(clock);
        DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper
            .paperResponseAdmissionLetter(ccdCase, "John S");
        DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
            .currentDate(LocalDate.parse("2020-06-22"))
            .partyName("Mary Richards")
            .partyAddress(CCDAddress.builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .addressLine3("line3")
                .postCode("postcode")
                .postTown("city")
                .build())
            .referenceNumber("ref no")
            .caseName("case name")
            .caseworkerName("John S")
            .build();
        assertThat(requestBody).isEqualTo(expectedBody);
    }

    @Nested
    @DisplayName("Test methods for bespoke direction orders")
    class BespokeOrderDirectionTests {

        @BeforeEach
        void setUp() {
            docAssemblyTemplateBodyMapper
                = new DocAssemblyTemplateBodyMapper(clock,
                directionOrderService,
                workingDayIndicator,
                responseDeadlineCalculator);
            ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
            ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);
            ccdCase.setHearingCourt("BIRMINGHAM");
            ccdCase.setCaseName("case name");
            ccdCase.setRespondents(
                ImmutableList.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(SampleData.getIndividualRespondentWithDQ())
                        .build()
                ));
            ccdCase.setApplicants(
                ImmutableList.of(
                    CCDCollectionElement.<CCDApplicant>builder()
                        .value(SampleData.getCCDApplicantIndividual())
                        .build()
                ));
            ccdCase.setDrawBespokeDirectionOrderWarning(ImmutableList.of(CCDBespokeOrderWarning.WARNING));
            ccdCase.setBespokeDirectionList(SampleData.getBespokeDirectionList());
            letterContent = GeneralLetterContent.builder()
                .letterContent(LETTER_CONTENT)
                .caseworkerName("Judge McJudge");

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
                .bespokeOrderWarning(true)
                .bespokeDirectionList(
                    ImmutableList.of(BespokeDirection
                            .builder()
                            .directionComment("first direction")
                            .forParty(CLAIMANT)
                            .sendBy(LocalDate.parse("2020-08-04"))
                            .build(),
                        BespokeDirection
                            .builder()
                            .directionComment("second direction")
                            .forParty(DEFENDANT)
                            .sendBy(LocalDate.parse("2020-08-04"))
                            .build(),
                        BespokeDirection
                            .builder()
                            .directionComment("third direction")
                            .forParty(BOTH)
                            .sendBy(LocalDate.parse("2020-08-04"))
                            .build()))
                .expertReportPermissionPartyAskedByClaimant(true)
                .expertReportPermissionPartyAskedByDefendant(true)
                .expertReportInstructionClaimant(Collections.emptyList())
                .expertReportInstructionDefendant(Collections.emptyList())
                .grantExpertReportPermission(true)
                .expertReportInstruction(SUBMIT_MORE_DOCS_INSTRUCTION);
        }

        @Test
        void shouldMapTemplateBodyWhenBespokeOrderDrawn() {
            when(clock.instant()).thenReturn(LocalDate.parse("2020-08-04").atStartOfDay().toInstant(ZoneOffset.UTC));
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);
            when(clock.withZone(LocalDateTimeFactory.UTC_ZONE)).thenReturn(clock);
            DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper
                .mapBespokeDirectionOrder(ccdCase, userDetails);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .judicial(Judicial.builder().firstName("Judge").lastName("McJudge").build())
                .claimant(Party.builder().partyName("Individual").build())
                .defendant(Party.builder().partyName("Mary Richards").build())
                .referenceNumber("ref no")
                .currentDate(LocalDate.parse("2020-08-04"))
                .bespokeOrderWarning(true)
                .bespokeDirectionList(
                    ImmutableList.of(BespokeDirection
                            .builder()
                            .directionComment("first direction")
                            .forParty(CLAIMANT)
                            .sendBy(LocalDate.parse("2020-08-04"))
                            .build(),
                        BespokeDirection
                            .builder()
                            .directionComment("second direction")
                            .forParty(DEFENDANT)
                            .sendBy(LocalDate.parse("2020-08-04"))
                            .build(),
                        BespokeDirection
                            .builder()
                            .directionComment("third direction")
                            .forParty(BOTH)
                            .sendBy(LocalDate.parse("2020-08-04"))
                            .build()))
                .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }

    }
}
