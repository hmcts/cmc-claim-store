package uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.PebbleConfiguration;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;

public class SettlementCountersignedEmailContentProviderTest {

    private final TemplateService templateService = new TemplateService(
        new PebbleConfiguration().pebbleEngine()
    );

    private final StaffEmailTemplates templates = new StaffEmailTemplates();

    private Claim claimWithSettlement;

    private final SettlementCountersignedEmailContentProvider classToTest =
        new SettlementCountersignedEmailContentProvider(templateService, templates);

    @Before
    public void setUp() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        claimWithSettlement = SampleClaim
            .builder()
            .withSettlementReachedAt(LocalDateTime.now())
            .withResponse(SampleResponse.validDefaults())
            .withSettlement(settlement)
            .build();
    }

    @Test
    public void shouldCreateStaffSettlementEmail() {
        EmailContent content = classToTest.createContent(ImmutableMap.of(
            CLAIMANT_NAME, claimWithSettlement.getClaimData().getClaimant().getName(),
            DEFENDANT_NAME, claimWithSettlement.getClaimData().getDefendant().getName(),
            CLAIM_REFERENCE_NUMBER, claimWithSettlement.getReferenceNumber()
        ));
        assertThat(content.getSubject())
            .isEqualTo(String.format("Settlement agreement signed by both parties: %s: %s v %s",
                claimWithSettlement.getReferenceNumber(),
                claimWithSettlement.getClaimData().getClaimant().getName(),
                claimWithSettlement.getClaimData().getDefendant().getName()));

        assertThat(content.getBody())
            .contains("The claimant and defendant have signed a settlement agreement to settle the claim.")
            .contains("This email has been sent from the HMCTS Civil Money Claims online court.");
    }
}
