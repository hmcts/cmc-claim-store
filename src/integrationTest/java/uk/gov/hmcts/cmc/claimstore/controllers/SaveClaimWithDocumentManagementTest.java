package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.document.utils.InMemoryMultipartFile;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;

@TestPropertySource(
    properties = {
        "feature_toggles.document_management=true"
    }
)
public class SaveClaimWithDocumentManagementTest extends BaseIntegrationTest {

    public static final String AUTHORISATION_TOKEN = "Bearer token";

    @Before
    public void setup() {
        given(userService.getUserDetails(AUTHORISATION_TOKEN))
            .willReturn(SampleUserDetails.builder().withUserId("1").withMail("claimant@email.com").build());

        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(new byte[]{1, 2, 3, 4});

        given(documentUploadClient.upload(eq(AUTHORISATION_TOKEN), anyList()))
            .willReturn(successfulDocumentManagementUploadResponse());
    }

    @Test
    public void shouldUploadSealedRepresentedClaimIntoDocumentManagementService() throws Exception {
        ClaimData claimData = SampleClaimData.builder()
            .withAmount(SampleAmountRange.validDefaults())
            .build();

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        Claim savedClaim = deserializeObjectFrom(result, Claim.class);

        verify(documentUploadClient).upload(AUTHORISATION_TOKEN, newArrayList(new InMemoryMultipartFile("files",
            savedClaim.getReferenceNumber() + ".pdf", "application/pdf",
            new byte[]{1, 2, 3, 4})
        ));
    }

    private ResultActions makeRequest(ClaimData claimData) throws Exception {
        return webClient
            .perform(post("/claims/" + (Long) 123L)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMapper.toJson(claimData))
            );
    }

}
