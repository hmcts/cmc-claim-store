package uk.gov.hmcts.cmc.claimstore.tests.functional.solicitor;

import org.junit.Before;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BaseSubmitClaimTest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.function.Supplier;

public class SolicitorSubmitClaimTest extends BaseSubmitClaimTest {

    @Before
    public void before() {
        user = idamTestService.createSolicitor();
    }

    @Override
    protected Supplier<SampleClaimData> getSampleClaimDataBuilder() {
        return testData::submittedBySolicitorBuilder;
    }
}
