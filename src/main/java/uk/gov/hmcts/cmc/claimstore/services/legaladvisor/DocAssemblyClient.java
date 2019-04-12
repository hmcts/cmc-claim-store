package uk.gov.hmcts.cmc.claimstore.services.legaladvisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingCourtType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirection;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DocAssemblyClient {
    private static final String TEMPLATE_NAME = "CV-CMC-GOR-ENG-0004.docx";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DocAssemblyApi docAssemblyApi;
    private final UserService userService;
    private final AuthTokenGenerator authTokenGenerator;
    private final JsonMapper jsonMapper;

    @Autowired
    public DocAssemblyClient(
        DocAssemblyApi docAssemblyApi,
        UserService userService,
        AuthTokenGenerator authTokenGenerator,
        JsonMapper jsonMapper) {
        this.docAssemblyApi = docAssemblyApi;
        this.userService = userService;
        this.authTokenGenerator = authTokenGenerator;
        this.jsonMapper = jsonMapper;
    }

    public DocAssemblyResponse  generateOrder(String authorisation, Map<String, Object> caseData) {
        String templateId = Base64.getEncoder()
            .encodeToString(TEMPLATE_NAME.getBytes());
        UserDetails userDetails = userService.getUserDetails(authorisation);
        CCDCase ccdCase = jsonMapper.fromMap(caseData, CCDCase.class);
        DocAssemblyRequest docAssemblyRequest = buildRequest(templateId, ccdCase, userDetails);
        try {
            return docAssemblyApi.generateOrder(
                docAssemblyRequest,
                authorisation,
                authTokenGenerator.generate());
        } catch (Exception e) {
            logger.error("Error while trying to generate an order with docAssembly");
            throw new DocumentGenerationFailedException(e);
        }
    }

    private DocAssemblyRequest buildRequest(String templateId, CCDCase ccdCase, UserDetails userDetails) {
        List<CCDOrderDirectionType> directionList = ccdCase.getDirectionList();
        return DocAssemblyRequest.builder()
            .templateId(templateId)
            .formPayload(
                DocAssemblyRequest.FormPayload.builder()
                    .referenceNumber(ccdCase.getReferenceNumber())
                    .claimant(DocAssemblyRequest.Party.builder()
                        .partyName(ccdCase.getApplicants()
                            .get(0)
                            .getValue()
                            .getPartyName())
                        .build())
                    .defendant(DocAssemblyRequest.Party.builder()
                        .partyName(ccdCase.getRespondents()
                            .get(0)
                            .getValue()
                            .getPartyName())
                        .build())
                    .judicial(DocAssemblyRequest.Judicial.builder()
                        .firstName(userDetails.getForename())
                        .lastName(userDetails.getSurname().orElse(""))
                        .build())
                    .currentDate(LocalDate.now())
                    .hasFirstOrderDirections(
                        directionList.contains(CCDOrderDirectionType.DOCUMENTS))
                    .docUploadDeadline(
                        ccdCase.getDocUploadDeadline())
                    .hasSecondOrderDirections(
                        directionList.contains(CCDOrderDirectionType.EYEWITNESS))
                    .eyewitnessUploadDeadline(
                        ccdCase.getEyewitnessUploadDeadline())
                    .hasThirdOrderDirections(
                        directionList.contains(CCDOrderDirectionType.MEDIATION))
                    .docUploadForParty(
                        ccdCase.getDocUploadForParty())
                    .eyewitnessUploadForParty(
                        ccdCase.getEyewitnessUploadForParty())
                    .mediationForParty(
                        ccdCase.getMediationForParty())
                    .hearingIsRequired(
                        ccdCase.getHearingIsRequired().toBoolean())
                    .preferredCourtName(
                        getCourtName(
                            ccdCase.getHearingCourt()))
                    .preferredCourtAddress(
                        "this is an address EC2 BLA")// will be populated when the acceptance criteria are refined
                    .estimatedHearingDuration(
                        ccdCase.getEstimatedHearingDuration())
                    .hearingStatement(
                        ccdCase.getHearingStatement())
                    .otherDirectionList(
                        ccdCase.getOtherDirectionList().stream().map(
                            direction -> CCDOrderDirection.builder()
                                .extraOrderDirection(direction.getExtraOrderDirection())
                                .otherDirection(direction.getOtherDirection())
                                .sendBy(direction.getSendBy())
                                .forParty(direction.getForParty())
                                .build()
                        ).collect(Collectors.toList()))
                    .build()
            )
            .build();
    }

    // will be populated when the acceptance criteria are refined
    private String getCourtName(CCDHearingCourtType ccdHearingCourtType) {
        switch (ccdHearingCourtType) {
            case EDMONTON:
                return "Edmonton court";
            case MANCHESTER:
                return "Manchester court";
            default:
                return "Some court";
        }
    }
}
