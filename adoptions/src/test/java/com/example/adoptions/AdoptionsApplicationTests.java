package com.example.adoptions;

import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@SpringBootTest
class AdoptionsApplicationTests {

	@Test
	void contextLoads() {
		var am = ApplicationModules.of (AdoptionsApplication.class);
		am .verify() ;

		System.out.println(am);

		new Documenter(am).writeDocumentation();
	}

}
