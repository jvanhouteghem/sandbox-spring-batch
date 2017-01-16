package principal;

import java.io.File;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;

// voir http://www.codingpedia.org/ama/spring-batch-tutorial-with-spring-boot-and-java-configuration/

@Configuration
@EnableBatchProcessing // @EnableBatchProcessing means that when it’s done, the data is gone.
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
        //reader.setLinesToSkip(1);//first line is title definition
        reader.setResource(new ClassPathResource("sample-data.csv"));
        
        // Map csv
        //The LineMapper is an interface for mapping lines (strings) to domain objects, typically used to map lines read from a file to domain objects on a per line basis.
        reader.setLineMapper(
        	new DefaultLineMapper<Person>() {{
        		
        		//Columns names
        		setLineTokenizer(new DelimitedLineTokenizer() {{setNames(new String[] { "firstName", "lastName" });}});
        		
        		//The FieldSetMapper is an interface that is used to map data obtained from a FieldSet into an objec
        		setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{setTargetType(Person.class);}});
        }});
        return reader;
    }

    //ItemProcessor is an abstraction that represents the business processing of an item. 
    //While the ItemReader reads one item, and the ItemWriter writes them, 
    //the ItemProcessor provides access to transform or apply other business processing
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
    
    // source : http://www.programcreek.com/java-api-examples/index.php?api=org.springframework.batch.item.file.FlatFileItemWriter
    @Bean
    public ItemWriter<Person> writer() {
    	FlatFileItemWriter<Person> writer = new FlatFileItemWriter<Person>();
		writer.setResource(new FileSystemResource(new File("target/out-javaconfig.txt")));
    	writer.setLineAggregator(new PassThroughLineAggregator<Person>());
    	return writer;
    }
     

    @Bean
    public Job importUserJob(JobListener jobListener) {
        return jobBuilderFactory.get("importUserJob")
                .start(step1())
                //.next(step2())
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
    
    /**
	The first chunk of code defines the input, processor, and output. 
	reader() creates an ItemReader. It looks for a file called sample-data.csv and parses each line item with enough information to turn it into a Person. 
	processor() creates an instance of our PersonItemProcessor you defined earlier, meant to uppercase the data. - 
     */
    @Bean
    public Step step1() {
    	
        return stepBuilderFactory.get("step1")
                .<Person, Person> chunk(10) // chunk() is prefixed <Person,Person> because it’s a generic method. This represents the input and output types of each "chunk" of processing, and lines up with ItemReader<Person> and ItemWriter<Person>. 
                .reader(personReader())
                .processor(personProcessor())
                .writer(writer())
                .build();
    }
    
    /*@Bean
    public Step step2() {
    	
        return stepBuilderFactory.get("step2")
                .<Person, Person> chunk(10)
                .reader(personReader())
                .processor(personProcessor())
                //.writer(writer())
                .build();
    }*/
    
}
