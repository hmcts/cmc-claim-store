package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
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

@RunWith(MockitoJUnitRunner.class)
public class DocAssemblyTemplateBodyMapperTest  {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Clock clock;

    private DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;

    @Before
    public void setUp() {
        docAssemblyTemplateBodyMapper = new DocAssemblyTemplateBodyMapper(clock);
    }

    @Test
    public void shouldSerialiseTemplateBodyToJson() throws JsonProcessingException {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        CCDOrderGenerationData getCCDOrderGenerationData = SampleData.getCCDOrderGenerationData();
        UserDetails userDetails = SampleUserDetails.builder()
            .withForename("Judge")
            .withSurname("McJudge")
            .build();
        //when
        Mockito.when(clock.instant()).thenReturn(LocalDate.parse("2019-04-24")
            .atStartOfDay().toInstant(ZoneOffset.UTC));
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);
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

}
