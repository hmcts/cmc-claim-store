package uk.gov.hmcts.cmc.dm.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cmc.dm.batch.enums.JobName;
import uk.gov.hmcts.cmc.dm.batch.listener.JobCompletionNotificationListener;
import uk.gov.hmcts.cmc.dm.batch.model.Claim;
import uk.gov.hmcts.cmc.dm.batch.writer.SealedClaimUploadWriter;

import javax.sql.DataSource;

@Configuration
public class SealedClaimUploadConfig {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    @Autowired
    private SealedClaimUploadWriter sealedClaimUploadWriter;
    @Autowired
    private JobCompletionNotificationListener jobCompletionNotificationListener;
    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcCursorItemReader<Claim> sealedClaimReader() {
        return new JdbcCursorItemReaderBuilder<Claim>()
            .name("sealedClaimReader")
            .sql("select id,reference_number,external_id from claim where "
                + "sealed_claim_document_management_self_path is null")
            .beanRowMapper(Claim.class)
            .dataSource(dataSource)
            .build();
    }

    @Bean
    public Step uploadSealedClaimToDocumentManagement() {
        return stepBuilderFactory.get("uploadSealedClaimToDocumentManagement")
            .<Claim, Claim>chunk(10)
            .reader(sealedClaimReader())
            .writer(sealedClaimUploadWriter)
            .build();
    }

    @Bean
    public Job uploadSealedClaimToDocumentManagementJob(Step uploadSealedClaimToDocumentManagement) {
        return jobBuilderFactory.get(JobName.UPLOAD_SEALED_CLAIM.toString())
            .incrementer(new RunIdIncrementer())
            .listener(jobCompletionNotificationListener)
            .flow(uploadSealedClaimToDocumentManagement)
            .end()
            .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
