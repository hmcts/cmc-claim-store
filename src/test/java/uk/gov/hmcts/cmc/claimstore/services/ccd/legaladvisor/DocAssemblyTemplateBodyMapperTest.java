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
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingCourtType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingDurationType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirection;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;

@RunWith(MockitoJUnitRunner.class)
public class DocAssemblyTemplateBodyMapperTest  {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Clock clock;

    private DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    private CCDCase ccdCase;
    private CCDOrderGenerationData getCCDOrderGenerationData;
    private UserDetails userDetails;

    DocAssemblyTemplateBody.DocAssemblyTemplateBodyBuilder docAssemblyTemplateBodyBuilder;

    @Before
    public void setUp() {
        docAssemblyTemplateBodyMapper = new DocAssemblyTemplateBodyMapper(clock);
        ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        getCCDOrderGenerationData = SampleData.getCCDOrderGenerationData();
        userDetails = SampleUserDetails.builder()
            .withForename("Judge")
            .withSurname("McJudge")
            .build();
        docAssemblyTemplateBodyBuilder = DocAssemblyTemplateBody.builder()
            .hearingRequired(true)
            .hasFirstOrderDirections(true)
            .hasSecondOrderDirections(true)
            .docUploadDeadline(LocalDate.parse("2020-10-11"))
            .eyewitnessUploadDeadline(LocalDate.parse("2020-10-11"))
            .currentDate(LocalDate.parse("2019-04-24"))
            .hearingStatement("No idea")
            .claimant(Party.builder().partyName("Individual").build())
            .defendant(Party.builder().partyName("Mary Richards").build())
            .judicial(Judicial.builder().firstName("Judge").lastName("McJudge").build())
            .referenceNumber("ref no")
            .preferredCourtName("Some court")
            .preferredCourtAddress("this is an address EC2Y 3ND")
            .docUploadForParty(CCDDirectionPartyType.CLAIMANT)
            .eyewitnessUploadForParty(CCDDirectionPartyType.DEFENDANT)
            .estimatedHearingDuration(CCDHearingDurationType.FOUR_HOURS)
            .otherDirectionList(ImmutableList.of(
                OtherDirection.builder()
                    .sendBy(LocalDate.parse("2020-10-11"))
                    .directionComment("a direction")
                    .extraOrderDirection(CCDOrderDirectionType.OTHER)
                    .forParty(CCDDirectionPartyType.BOTH)
                    .build()
            ));
        //when
        Mockito.when(clock.instant()).thenReturn(LocalDate.parse("2019-04-24")
            .atStartOfDay().toInstant(ZoneOffset.UTC));
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    public void shouldSerialiseTemplateBodyToExpectedJson() throws JsonProcessingException {
        String output = objectMapper.writeValueAsString(
            docAssemblyTemplateBodyMapper.from(
                ccdCase,
                getCCDOrderGenerationData,
                userDetails));
        //then
        assertThat(output).isNotNull();
        String expected = new ResourceReader().read("/doc-assembly-template.json");
        JSONAssert.assertEquals(expected, output, STRICT);
    }

    @Test
    public void shouldMapTemplateBody() {
        DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.from(
            ccdCase,
            getCCDOrderGenerationData,
            userDetails);

        DocAssemblyTemplateBody expectedBody = docAssemblyTemplateBodyBuilder.build();

        assertThat(requestBody).isEqualTo(expectedBody);
    }

    @Test
    public void shouldMapTemplateBodyWhenOtherDirectionIsNull() {
        CCDOrderGenerationData ccdOrderGenerationData = CCDOrderGenerationData.builder()
            .directionList(ImmutableList.of(
                CCDOrderDirectionType.DOCUMENTS, CCDOrderDirectionType.EYEWITNESS))
            .otherDirectionList(ImmutableList.of(
                CCDCollectionElement.<CCDOrderDirection>builder().value(null).build()))
            .hearingIsRequired(YES)
            .docUploadDeadline(LocalDate.parse("2020-10-11"))
            .eyewitnessUploadDeadline(LocalDate.parse("2020-10-11"))
            .hearingCourt(CCDHearingCourtType.DEFENDANT_COURT)
            .preferredCourtObjectingReason("I like this court more")
            .hearingStatement("No idea")
            .newRequestedCourt("Another court")
            .docUploadForParty(CCDDirectionPartyType.CLAIMANT)
            .eyewitnessUploadForParty(CCDDirectionPartyType.DEFENDANT)
            .estimatedHearingDuration(CCDHearingDurationType.FOUR_HOURS)
            .build();

        DocAssemblyTemplateBody requestBody = docAssemblyTemplateBodyMapper.from(
            ccdCase,
            ccdOrderGenerationData,
            userDetails);

        docAssemblyTemplateBodyBuilder.otherDirectionList(Collections.emptyList());
        DocAssemblyTemplateBody expectedBody = docAssemblyTemplateBodyBuilder.build();

        assertThat(requestBody).isEqualTo(expectedBody);
    }

}
