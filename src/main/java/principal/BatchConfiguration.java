package principal;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Bean
    public FlatFileItemReader<Person> personReader() {
    	// Load csv
        FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
        reader.setResource(new ClassPathResource("sample-data.csv"));
        // Map csv
        reader.setLineMapper(
        	new DefaultLineMapper<Person>() {{
        		setLineTokenizer(new DelimitedLineTokenizer() {{
        			setNames(new String[] { "firstName", "lastName" });
        		}});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }});
        }});
        return reader;
    }

    @Bean
    public PersonProcessor personProcessor() {
        return new PersonProcessor();
    }

    /*@Bean
    public JdbcBatchItemWriter<Person> writer() {
        JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
        writer.setSql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)");
        writer.setDataSource(dataSource);
        return writer;
    }*/

    @Bean
    public Job importUserJob(JobListener jobListener) {
        return jobBuilderFactory.get("importUserJob")
                .start(step1())
                .next(step2())
                .build();
    }
    
    /*@Bean
    public Job importUserJob(JobListener jobListener) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .flow(step1())
                .end()
                .build();
    }*/

    @Bean
    public Step step1() {
    	
        return stepBuilderFactory.get("step1")
                .<Person, Person> chunk(10)
                .reader(personReader())
                .processor(personProcessor())
                //.writer(writer())
                .build();
    }
    
    @Bean
    public Step step2() {
    	
        return stepBuilderFactory.get("step2")
                .<Person, Person> chunk(10)
                .reader(personReader())
                .processor(personProcessor())
                //.writer(writer())
                .build();
    }    
    
}
