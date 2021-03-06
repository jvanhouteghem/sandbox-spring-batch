package principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;

public class PersonProcessor implements ItemProcessor<Person, Person> { //ItemProcessor<I, O>

	private static final Logger log = LoggerFactory.getLogger(PersonProcessor.class);

	@Override
	public Person process(final Person person) throws Exception {
		final String firstName = person.getCode();
		final String lastName = person.getLastName().toUpperCase();

		final Person transformedPerson = new Person(firstName, lastName);

		log.info("Converting (" + person + ") into (" + transformedPerson + ")");

		return transformedPerson;
	}

}
