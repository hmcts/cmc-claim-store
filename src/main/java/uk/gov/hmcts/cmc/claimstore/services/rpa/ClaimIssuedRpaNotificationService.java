package uk.gov.hmcts.cmc.claimstore.services.rpa;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.RpaEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.InvalidApplicationException;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.domain.Case;
import uk.gov.hmcts.cmc.rpa.mapper.CaseMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.processors.JsonMapper.SERIALISATION_ERROR_MESSAGE;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class ClaimIssuedRpaNotificationService {
    public static final String JSON_EXTENSION = ".json";
    private final EmailService emailService;
    private final RpaEmailProperties rpaEmailProperties;
    private final ClaimIssuedRpaNotificationEmailContentProvider provider;
    private final CaseMapper rpaCaseMapper;

    @Autowired
    public ClaimIssuedRpaNotificationService(
        EmailService emailService,
        RpaEmailProperties rpaEmailProperties,
        ClaimIssuedRpaNotificationEmailContentProvider provider,
        CaseMapper rpaCaseMapper
    ) {
        this.emailService = emailService;
        this.rpaEmailProperties = rpaEmailProperties;
        this.provider = provider;
        this.rpaCaseMapper = rpaCaseMapper;
    }

    @EventListener
    public void notifyRobotOfClaimIssue(DocumentGeneratedEvent event) {
        requireNonNull(event);

        EmailData emailData = prepareEmailData(event.getClaim(), event.getDocuments());
        emailService.sendEmail(rpaEmailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(
        Claim claim,
        List<PDF> documents) {
        EmailContent content = provider.createContent(wrapInMap(claim));

        List<EmailAttachment> attachments = documents.stream()
            .filter(document -> document.getFilename().contains("claim-form"))
            .map(document -> pdf(document.getBytes(), document.getFilename()))
            .collect(Collectors.toList());

        addJsonFileToAttachments(claim, attachments);

        return new EmailData(rpaEmailProperties.getRecipient(),
            content.getSubject(),
            content.getBody(),
            attachments);
    }

    private void addJsonFileToAttachments(Claim claim, List<EmailAttachment> attachments) {
        Case result = rpaCaseMapper.to(claim);

        attachments.add(EmailAttachment.json(toJson(result).getBytes(),
            DocumentNameUtils.buildJsonClaimFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION));
    }

    private String toJson(Case input) {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.ALWAYS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new InvalidApplicationException(
                String.format(SERIALISATION_ERROR_MESSAGE, input.getClass().getSimpleName()), e
            );
        }
    }

    public static Map<String, Object> wrapInMap(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        return map;
    }
}
