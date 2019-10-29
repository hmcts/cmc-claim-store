package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.domain.models.orders.DirectionOrder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDOrderGenerationData;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DirectionOrderMapperTest {

    @Autowired
    DirectionOrderMapper mapper;

    @Test
    public void shouldMapDirectionOrderFromCCD() {
        CCDDirectionOrder ccdDirectionOrder = CCDDirectionOrder.builder()
            .hearingCourtAddress(SampleData.getHearingCourtAddress())
            .hearingCourtName(SampleData.MANCHESTER_CIVIL_JUSTICE_CENTRE_CIVIL_AND_FAMILY_COURTS)
            .createdOn(LocalDateTime.now())
            .build();

        CCDOrderGenerationData orderGenerationData = getCCDOrderGenerationData();
        DirectionOrder directionOrder = mapper.from(ccdDirectionOrder, orderGenerationData);

        assertThat(directionOrder).isEqualTo(ccdDirectionOrder);
        assertThat(directionOrder.getDirections()).hasSize(4);

        assertEnumNames(directionOrder.getExpertReportPermissionGivenToClaimant(),
            orderGenerationData.getExpertReportPermissionPartyGivenToClaimant());

        assertEnumNames(directionOrder.getExpertReportPermissionGivenToDefendant(),
            orderGenerationData.getExpertReportPermissionPartyGivenToDefendant());

        assertEnumNames(directionOrder.getExpertReportPermissionAskedByClaimant(),
            orderGenerationData.getExpertReportPermissionPartyAskedByClaimant());

        assertEnumNames(directionOrder.getExpertReportPermissionAskedByDefendant(),
            orderGenerationData.getExpertReportPermissionPartyAskedByDefendant());

        directionOrder.getExpertReportInstructionsForClaimant()
            .forEach(assertInstructions(orderGenerationData.getExpertReportInstructionClaimant()));

        directionOrder.getExpertReportInstructionsForDefendant()
            .forEach(assertInstructions(orderGenerationData.getExpertReportInstructionDefendant()));
    }

    private void assertEnumNames(YesNoOption input, CCDYesNoOption expected) {
        assertThat(input.name()).isEqualTo(expected.name());
    }

    private Consumer<String> assertInstructions(List<CCDCollectionElement<String>> expertReportInstructions) {
        return instruction -> assertThat(expertReportInstructions
            .stream()
            .map(CCDCollectionElement::getValue)
            .anyMatch(value -> value.equals(instruction))
        ).isTrue();
    }

    @Test
    public void shouldMapNullCCDDirectionOrderFromCCD() {
        DirectionOrder directionOrder = mapper.from(null, getCCDOrderGenerationData());
        assertNull(directionOrder);
    }

    @Test
    public void shouldMapNullCCDOrderGenerationDataFromCCD() {
        CCDDirectionOrder ccdDirectionOrder = CCDDirectionOrder.builder()
            .hearingCourtAddress(SampleData.getHearingCourtAddress())
            .hearingCourtName(SampleData.MANCHESTER_CIVIL_JUSTICE_CENTRE_CIVIL_AND_FAMILY_COURTS)
            .createdOn(LocalDateTime.now())
            .build();

        DirectionOrder directionOrder = mapper.from(ccdDirectionOrder, null);
        assertNull(directionOrder);
    }
}
