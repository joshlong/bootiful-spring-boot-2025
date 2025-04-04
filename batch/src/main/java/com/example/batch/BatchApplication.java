package com.example.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    // jobs -> 0-N steps (readers,processors,writers)

    record Dog(int id, String name, String owner, String description) {
    }

    @Bean
    FlatFileItemReader<Dog> reader(@Value("file://${HOME}/Desktop/talk/dogs.csv") Resource resource) {
        return new FlatFileItemReaderBuilder<Dog>()
                .name("dogReader")
                .resource(resource)
                .delimited().names("id,name,description,dob,owner,gender,image".split(","))
                .linesToSkip(1)
                .fieldSetMapper(fieldSet -> new Dog(fieldSet.readInt("id"),
                        fieldSet.readString("name"),
                        fieldSet.readString("owner"),
                        fieldSet.readString("description")
                ))
                .build();
    }

    @Bean
    JdbcBatchItemWriter<Dog> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Dog>()

                .dataSource(dataSource)
                .sql("INSERT INTO DOG (ID, NAME, DESCRIPTION, OWNER) VALUES ( ?,?,?,? )")
                .assertUpdates(true)
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setInt(1, item.id());
                    ps.setString(2, item.name());
                    ps.setString(3, item.description());
                    ps.setString(4, item.owner());
                })
                .build();
    }

    @Bean
    Step step(JobRepository repository, PlatformTransactionManager tx,
              FlatFileItemReader<Dog> reader,
              JdbcBatchItemWriter<Dog> writer) {
        return new StepBuilder("step", repository)
                .<Dog, Dog>chunk(10, tx)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    Job job(JobRepository repository, Step step) {
        return new JobBuilder("job", repository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

}
